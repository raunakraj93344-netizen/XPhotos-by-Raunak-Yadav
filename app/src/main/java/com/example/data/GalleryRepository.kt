package com.example.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AccentPreset
import com.example.model.AppNameOption
import com.example.model.DeveloperSettings
import com.example.model.DeviceFolder
import com.example.model.ExifDetails
import com.example.model.FolderStorageStat
import com.example.model.GalleryStats
import com.example.model.PersistedState
import com.example.model.Photo
import com.example.model.ResolvedAlbum
import com.example.model.Settings
import com.example.model.UserAlbum
import com.example.util.GalleryUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant

private val Context.xphotosDataStore: DataStore<Preferences> by preferencesDataStore(name = "xphotos_offline_state")

class GalleryRepository(private val context: Context) {

    companion object {
        const val DEVELOPER_SECRET_CODE = "RAUNAKxYADAV"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val stateKey = stringPreferencesKey("xphotos_state_v2")

    val mediaSource = DeviceMediaDataSource(context)

    private val _persisted = MutableStateFlow(PersistedState())
    val persisted: StateFlow<PersistedState> = _persisted.asStateFlow()

    private val _devicePhotos = MutableStateFlow<List<Photo>>(emptyList())
    val devicePhotos: StateFlow<List<Photo>> = _devicePhotos.asStateFlow()

    private val _hydrated = MutableStateFlow(false)
    val hydrated: StateFlow<Boolean> = _hydrated.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _hasMediaPermission = MutableStateFlow(mediaSource.hasMediaPermission())
    val hasMediaPermission: StateFlow<Boolean> = _hasMediaPermission.asStateFlow()

    private val emojiCycle = listOf("📸", "🌟", "🌊", "🏔️", "🎨", "📁", "🎞️", "🖤")

    init {
        scope.launch {
            runCatching {
                val prefs = context.xphotosDataStore.data.first()
                val raw = prefs[stateKey]
                if (!raw.isNullOrBlank()) {
                    val loaded = json.decodeFromString<PersistedState>(raw)
                    val cleanedCustom = loaded.customPhotos.filter { photo ->
                        if (photo.uri.startsWith("file://")) {
                            File(Uri.parse(photo.uri).path.orEmpty()).exists()
                        } else true
                    }
                    _persisted.value = loaded.copy(customPhotos = cleanedCustom)
                }
            }
            _hydrated.value = true
            refreshDevicePhotos()
        }
    }

    fun refreshDevicePhotos() {
        val granted = mediaSource.hasMediaPermission()
        _hasMediaPermission.value = granted
        if (!granted) {
            _devicePhotos.value = emptyList()
            return
        }
        scope.launch {
            _isScanning.value = true
            val scanned = mediaSource.loadDevicePhotos()
            _devicePhotos.value = scanned
            _isScanning.value = false
        }
    }

    private fun updatePersisted(transform: (PersistedState) -> PersistedState) {
        val next = transform(_persisted.value)
        _persisted.value = next
        scope.launch {
            runCatching {
                val encoded = json.encodeToString(next)
                context.xphotosDataStore.edit { prefs ->
                    prefs[stateKey] = encoded
                }
            }
        }
    }

    fun allRawPhotos(
        state: PersistedState = _persisted.value,
        device: List<Photo> = _devicePhotos.value
    ): List<Photo> {
        val customUris = state.customPhotos.map { it.uri }.toSet()
        val dedupedDevice = device.filter { it.uri !in customUris }
        return (state.customPhotos + dedupedDevice).sortedByDescending { it.epochMillis }
    }

    fun allVisiblePhotos(
        state: PersistedState = _persisted.value,
        device: List<Photo> = _devicePhotos.value
    ): List<Photo> {
        val hidden = state.hiddenIds.toSet()
        return allRawPhotos(state, device).filter { it.id !in hidden }
    }

    fun hiddenPhotos(
        state: PersistedState = _persisted.value,
        device: List<Photo> = _devicePhotos.value
    ): List<Photo> {
        val hidden = state.hiddenIds.toSet()
        return allRawPhotos(state, device).filter { it.id in hidden }
    }

    fun deviceFolders(
        state: PersistedState = _persisted.value,
        device: List<Photo> = _devicePhotos.value
    ): List<DeviceFolder> {
        val visible = allVisiblePhotos(state, device)
        return visible
            .groupBy { it.bucketId }
            .map { (bucketId, list) ->
                DeviceFolder(
                    id = bucketId,
                    name = list.firstOrNull()?.bucketName ?: "Folder",
                    count = list.size,
                    coverUri = list.firstOrNull()?.thumb,
                    totalBytes = list.sumOf { p ->
                        if (p.sizeBytes > 0L) p.sizeBytes else GalleryUtils.estimateSize(p)
                    }
                )
            }
            .sortedByDescending { it.count }
    }

    fun computeStats(
        state: PersistedState = _persisted.value,
        device: List<Photo> = _devicePhotos.value
    ): GalleryStats {
        val visible = allVisiblePhotos(state, device)
        val folders = deviceFolders(state, device)
        val storage = visible.sumOf { p ->
            if (p.sizeBytes > 0L) p.sizeBytes else GalleryUtils.estimateSize(p)
        }
        val visibleIds = visible.map { it.id }.toSet()
        return GalleryStats(
            totalPhotos = visible.size,
            favorites = state.favorites.count { it in visibleIds },
            albums = state.userAlbums.size,
            hiddenCount = hiddenPhotos(state, device).size,
            storageBytes = storage,
            folderStats = folders.map {
                FolderStorageStat(
                    folderName = it.name,
                    count = it.count,
                    bytes = it.totalBytes
                )
            }
        )
    }

    suspend fun readExif(photo: Photo): ExifDetails = mediaSource.readExif(photo)

    fun toggleFavorite(id: String) {
        updatePersisted { prev ->
            val updated = if (id in prev.favorites) {
                prev.favorites.filter { it != id }
            } else {
                listOf(id) + prev.favorites
            }
            prev.copy(favorites = updated)
        }
    }

    fun setFavoriteForMany(ids: List<String>, favorite: Boolean) {
        val targetSet = ids.toSet()
        updatePersisted { prev ->
            val updated = if (favorite) {
                (ids.filter { it !in prev.favorites } + prev.favorites).distinct()
            } else {
                prev.favorites.filter { it !in targetSet }
            }
            prev.copy(favorites = updated)
        }
    }

    fun hidePhotos(ids: List<String>) {
        if (ids.isEmpty()) return
        updatePersisted { prev ->
            prev.copy(hiddenIds = (prev.hiddenIds + ids).distinct())
        }
    }

    fun unhidePhotos(ids: List<String>) {
        if (ids.isEmpty()) return
        val removeSet = ids.toSet()
        updatePersisted { prev ->
            prev.copy(hiddenIds = prev.hiddenIds.filter { it !in removeSet })
        }
    }

    fun createAlbum(title: String, emoji: String): String {
        val id = "album-${System.currentTimeMillis()}"
        val album = UserAlbum(
            id = id,
            title = title.trim().ifEmpty { "Untitled album" },
            emoji = emoji.ifEmpty { emojiCycle.random() },
            photoIds = emptyList(),
            createdAt = Instant.now().toString()
        )
        updatePersisted { prev ->
            prev.copy(userAlbums = listOf(album) + prev.userAlbums)
        }
        return id
    }

    fun deleteAlbum(id: String) {
        updatePersisted { prev ->
            prev.copy(userAlbums = prev.userAlbums.filter { it.id != id })
        }
    }

    fun renameAlbum(id: String, title: String) {
        val trimmed = title.trim()
        updatePersisted { prev ->
            prev.copy(
                userAlbums = prev.userAlbums.map { album ->
                    if (album.id == id && trimmed.isNotEmpty()) album.copy(title = trimmed) else album
                }
            )
        }
    }

    fun addPhotosToAlbum(albumId: String, photoIds: List<String>) {
        updatePersisted { prev ->
            prev.copy(
                userAlbums = prev.userAlbums.map { album ->
                    if (album.id == albumId) {
                        album.copy(photoIds = (album.photoIds + photoIds).distinct())
                    } else album
                }
            )
        }
    }

    fun removePhotosFromAlbum(albumId: String, photoIds: List<String>) {
        val removeSet = photoIds.toSet()
        updatePersisted { prev ->
            prev.copy(
                userAlbums = prev.userAlbums.map { album ->
                    if (album.id == albumId) {
                        album.copy(photoIds = album.photoIds.filter { it !in removeSet })
                    } else album
                }
            )
        }
    }

    fun addCapturedPhoto(
        uri: String,
        title: String = "Captured Photo",
        width: Int = 1080,
        height: Int = 1440,
        mimeType: String = "image/jpeg"
    ): Photo {
        val now = System.currentTimeMillis()
        val file = if (uri.startsWith("file://")) File(Uri.parse(uri).path.orEmpty()) else null
        val sizeBytes = file?.takeIf { it.exists() }?.length() ?: 0L
        val photo = Photo(
            id = "c-$now-${(100..999).random()}",
            mediaStoreId = null,
            uri = uri,
            thumb = uri,
            width = width,
            height = height,
            title = title,
            bucketId = "app_captures",
            bucketName = "In-App Captures",
            relativePath = file?.absolutePath ?: uri,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            epochMillis = now,
            isCustom = true
        )
        updatePersisted { prev ->
            prev.copy(customPhotos = listOf(photo) + prev.customPhotos)
        }
        return photo
    }

    fun removePhotoReferences(ids: List<String>) {
        val removeSet = ids.toSet()
        _devicePhotos.value = _devicePhotos.value.filter { it.id !in removeSet }
        updatePersisted { prev ->
            prev.copy(
                favorites = prev.favorites.filter { it !in removeSet },
                customPhotos = prev.customPhotos.filter { it.id !in removeSet },
                hiddenIds = prev.hiddenIds.filter { it !in removeSet },
                userAlbums = prev.userAlbums.map { album ->
                    album.copy(photoIds = album.photoIds.filter { it !in removeSet })
                }
            )
        }
    }

    suspend fun requestDeleteFromDevice(photos: List<Photo>): DeleteOutcome {
        val outcome = mediaSource.deletePhotosFromDevice(photos)
        if (outcome is DeleteOutcome.Deleted) {
            removePhotoReferences(photos.map { it.id })
        }
        return outcome
    }

    fun recordSearch(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        updatePersisted { prev ->
            val next = (listOf(q) + prev.recentSearches.filter { !it.equals(q, ignoreCase = true) }).take(8)
            prev.copy(recentSearches = next)
        }
    }

    fun clearRecentSearches() {
        updatePersisted { prev -> prev.copy(recentSearches = emptyList()) }
    }

    fun updateSettings(transform: (Settings) -> Settings) {
        updatePersisted { prev -> prev.copy(settings = transform(prev.settings)) }
    }

    fun updateDeveloperSettings(transform: (DeveloperSettings) -> DeveloperSettings) {
        updatePersisted { prev -> prev.copy(developerSettings = transform(prev.developerSettings)) }
    }

    fun unlockDeveloperOptions(enteredCode: String): Boolean {
        val valid = enteredCode.trim() == DEVELOPER_SECRET_CODE
        if (valid) {
            updateDeveloperSettings { it.copy(unlocked = true) }
        }
        return valid
    }

    fun lockDeveloperOptions() {
        updateDeveloperSettings {
            it.copy(
                unlocked = false,
                showFpsRamHud = false
            )
        }
    }

    fun switchAppName(option: AppNameOption) {
        updateDeveloperSettings { it.copy(appName = option) }
        runCatching {
            val pm = context.packageManager
            val targetComponent = ComponentName(context, option.aliasClassName)
            pm.setComponentEnabledSetting(
                targetComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            AppNameOption.entries.forEach { other ->
                if (other != option) {
                    pm.setComponentEnabledSetting(
                        ComponentName(context, other.aliasClassName),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }
        }
    }

    fun setVaultPin(pin: String) {
        val hash = GalleryUtils.sha256(pin)
        updatePersisted { it.copy(vaultPinHash = hash) }
    }

    fun verifyVaultPin(pin: String): Boolean {
        val stored = _persisted.value.vaultPinHash ?: return false
        return GalleryUtils.sha256(pin) == stored
    }

    fun clearVaultPin() {
        updatePersisted { it.copy(vaultPinHash = null) }
    }

    fun resetData() {
        switchAppName(AppNameOption.XPhotos)
        updatePersisted { PersistedState() }
    }

    fun resolveAlbum(
        albumId: String,
        state: PersistedState = _persisted.value,
        device: List<Photo> = _devicePhotos.value
    ): ResolvedAlbum? {
        val visible = allVisiblePhotos(state, device)
        val appLabel = state.developerSettings.appName.displayName
        return when {
            albumId == "favorites" -> {
                val favSet = state.favorites.toSet()
                val items = visible.filter { it.id in favSet }
                ResolvedAlbum(
                    id = "favorites",
                    title = "Favorites",
                    subtitle = "Your starred local shots",
                    emoji = "❤️",
                    gradientColors = listOf(Color(0xFFFF5C8A), Color(0xFFFF8A5C)),
                    photos = items,
                    photoIds = items.map { it.id },
                    isUser = false,
                    isSmart = true
                )
            }
            albumId == "recent" -> {
                val items = visible.take(30)
                ResolvedAlbum(
                    id = "recent",
                    title = "Recently Added",
                    subtitle = "Latest photos on your device",
                    emoji = "🕒",
                    gradientColors = listOf(Color(0xFF2E7BFF), Color(0xFF21D4C4)),
                    photos = items,
                    photoIds = items.map { it.id },
                    isUser = false,
                    isSmart = true
                )
            }
            albumId == "camera" -> {
                val items = visible.filter {
                    it.isCustom || it.bucketName.equals("Camera", ignoreCase = true)
                }
                ResolvedAlbum(
                    id = "camera",
                    title = "Camera Roll",
                    subtitle = "Device & $appLabel captures",
                    emoji = "📷",
                    gradientColors = listOf(Color(0xFF2B2B38), Color(0xFF5C5C72)),
                    photos = items,
                    photoIds = items.map { it.id },
                    isUser = false,
                    isSmart = true
                )
            }
            albumId == "all" -> {
                ResolvedAlbum(
                    id = "all",
                    title = "All Photos",
                    subtitle = "Everything stored on device",
                    emoji = "🗂️",
                    gradientColors = listOf(Color(0xFF7C5CFF), Color(0xFFC05CFF)),
                    photos = visible,
                    photoIds = visible.map { it.id },
                    isUser = false,
                    isSmart = true
                )
            }
            albumId.startsWith("folder:") -> {
                val bucketId = albumId.removePrefix("folder:")
                val items = visible.filter { it.bucketId == bucketId }
                val folderName = items.firstOrNull()?.bucketName ?: "Folder"
                ResolvedAlbum(
                    id = albumId,
                    title = folderName,
                    subtitle = "Device folder",
                    emoji = "📁",
                    gradientColors = listOf(Color(0xFF1F8A5B), Color(0xFF21D4C4)),
                    photos = items,
                    photoIds = items.map { it.id },
                    isUser = false,
                    isSmart = false,
                    isFolder = true
                )
            }
            else -> {
                val user = state.userAlbums.find { it.id == albumId } ?: return null
                val idSet = user.photoIds.toSet()
                val items = visible.filter { it.id in idSet }
                ResolvedAlbum(
                    id = user.id,
                    title = user.title,
                    subtitle = "${items.size} ${if (items.size == 1) "photo" else "photos"}",
                    emoji = user.emoji,
                    gradientColors = listOf(Color(0xFF7C5CFF), Color(0xFFC05CFF)),
                    photos = items,
                    photoIds = items.map { it.id },
                    isUser = true,
                    isSmart = false
                )
            }
        }
    }
}
