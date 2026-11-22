package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DeleteOutcome
import com.example.data.GalleryRepository
import com.example.model.AccentPreset
import com.example.model.AppNameOption
import com.example.model.DeveloperSettings
import com.example.model.DeviceFolder
import com.example.model.ExifDetails
import com.example.model.GalleryStats
import com.example.model.GridDensity
import com.example.model.PersistedState
import com.example.model.Photo
import com.example.model.ResolvedAlbum
import com.example.model.Settings
import com.example.model.SortOption
import com.example.model.ThemeMode
import com.example.model.UserAlbum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GalleryUiState(
    val hydrated: Boolean = false,
    val isScanning: Boolean = false,
    val hasMediaPermission: Boolean = false,
    val photos: List<Photo> = emptyList(),
    val hiddenPhotos: List<Photo> = emptyList(),
    val deviceFolders: List<DeviceFolder> = emptyList(),
    val favorites: List<String> = emptyList(),
    val userAlbums: List<UserAlbum> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val settings: Settings = Settings(),
    val developerSettings: DeveloperSettings = DeveloperSettings(),
    val hasVaultPin: Boolean = false,
    val stats: GalleryStats = GalleryStats(),
    val persisted: PersistedState = PersistedState(),
    val rawDevicePhotos: List<Photo> = emptyList()
) {
    val appNameLabel: String
        get() = developerSettings.appName.displayName

    fun getPhoto(id: String?): Photo? =
        if (id == null) null else photos.find { it.id == id } ?: hiddenPhotos.find { it.id == id }

    fun isFavorite(id: String): Boolean = id in favorites
}

class GalleryViewModel(private val repository: GalleryRepository) : ViewModel() {

    private val _pendingDeleteIds = MutableStateFlow<List<String>>(emptyList())

    val uiState: StateFlow<GalleryUiState> = combine(
        repository.persisted,
        repository.devicePhotos,
        repository.hydrated,
        repository.isScanning,
        repository.hasMediaPermission
    ) { persisted, device, hydrated, scanning, hasPerm ->
        GalleryUiState(
            hydrated = hydrated,
            isScanning = scanning,
            hasMediaPermission = hasPerm,
            photos = repository.allVisiblePhotos(persisted, device),
            hiddenPhotos = repository.hiddenPhotos(persisted, device),
            deviceFolders = repository.deviceFolders(persisted, device),
            favorites = persisted.favorites,
            userAlbums = persisted.userAlbums,
            recentSearches = persisted.recentSearches,
            settings = persisted.settings,
            developerSettings = persisted.developerSettings,
            hasVaultPin = !persisted.vaultPinHash.isNullOrBlank(),
            stats = repository.computeStats(persisted, device),
            persisted = persisted,
            rawDevicePhotos = device
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GalleryUiState()
    )

    fun refreshDevicePhotos() = repository.refreshDevicePhotos()

    fun resolveAlbum(albumId: String): ResolvedAlbum? =
        repository.resolveAlbum(albumId, uiState.value.persisted, uiState.value.rawDevicePhotos)

    suspend fun readExif(photo: Photo): ExifDetails = repository.readExif(photo)

    fun toggleFavorite(id: String) = repository.toggleFavorite(id)

    fun setFavoriteForMany(ids: List<String>, favorite: Boolean) =
        repository.setFavoriteForMany(ids, favorite)

    fun hidePhotos(ids: List<String>) = repository.hidePhotos(ids)

    fun unhidePhotos(ids: List<String>) = repository.unhidePhotos(ids)

    fun createAlbum(title: String, emoji: String): String =
        repository.createAlbum(title, emoji)

    fun deleteAlbum(id: String) = repository.deleteAlbum(id)

    fun renameAlbum(id: String, title: String) = repository.renameAlbum(id, title)

    fun addPhotosToAlbum(albumId: String, photoIds: List<String>) =
        repository.addPhotosToAlbum(albumId, photoIds)

    fun removePhotosFromAlbum(albumId: String, photoIds: List<String>) =
        repository.removePhotosFromAlbum(albumId, photoIds)

    fun addCapturedPhoto(
        uri: String,
        title: String = "Captured Photo",
        width: Int = 1080,
        height: Int = 1440,
        mimeType: String = "image/jpeg"
    ): Photo = repository.addCapturedPhoto(uri, title, width, height, mimeType)

    fun requestDeletePhotos(
        photos: List<Photo>,
        onResult: (DeleteOutcome) -> Unit
    ) {
        _pendingDeleteIds.value = photos.map { it.id }
        viewModelScope.launch {
            val outcome = repository.requestDeleteFromDevice(photos)
            if (outcome !is DeleteOutcome.RequiresConfirmation) {
                _pendingDeleteIds.value = emptyList()
            }
            onResult(outcome)
        }
    }

    fun onDeviceDeleteConfirmed(approved: Boolean) {
        val pending = _pendingDeleteIds.value
        _pendingDeleteIds.value = emptyList()
        if (approved && pending.isNotEmpty()) {
            repository.removePhotoReferences(pending)
            repository.refreshDevicePhotos()
        }
    }

    fun recordSearch(query: String) = repository.recordSearch(query)

    fun clearRecentSearches() = repository.clearRecentSearches()

    fun setThemeMode(mode: ThemeMode) = repository.updateSettings { it.copy(themeMode = mode) }

    fun setDensity(density: GridDensity) = repository.updateSettings { it.copy(density = density) }

    fun setSort(sort: SortOption) = repository.updateSettings { it.copy(sort = sort) }

    fun setAutoplay(autoplay: Boolean) = repository.updateSettings { it.copy(autoplay = autoplay) }

    fun setShowMeta(showMeta: Boolean) = repository.updateSettings { it.copy(showMeta = showMeta) }

    // Developer Options actions
    fun unlockDeveloperOptions(code: String): Boolean = repository.unlockDeveloperOptions(code)

    fun lockDeveloperOptions() = repository.lockDeveloperOptions()

    fun switchAppName(option: AppNameOption) = repository.switchAppName(option)

    fun setShowFpsRamHud(enabled: Boolean) =
        repository.updateDeveloperSettings { it.copy(showFpsRamHud = enabled) }

    fun setShowRawExifAndPath(enabled: Boolean) =
        repository.updateDeveloperSettings { it.copy(showRawExifAndPath = enabled) }

    fun setAccentPreset(preset: AccentPreset) =
        repository.updateDeveloperSettings { it.copy(accentPreset = preset) }

    fun setPureAmoledDark(enabled: Boolean) =
        repository.updateDeveloperSettings { it.copy(pureAmoledDark = enabled) }

    fun setAnimationSpeedScale(scale: Float) =
        repository.updateDeveloperSettings { it.copy(animationSpeedScale = scale) }

    fun setSlideshowIntervalMs(ms: Int) =
        repository.updateDeveloperSettings { it.copy(slideshowIntervalMs = ms) }

    fun setSlideshowShuffle(shuffle: Boolean) =
        repository.updateDeveloperSettings { it.copy(slideshowShuffle = shuffle) }

    // Hidden Vault PIN actions
    fun setVaultPin(pin: String) = repository.setVaultPin(pin)

    fun verifyVaultPin(pin: String): Boolean = repository.verifyVaultPin(pin)

    fun clearVaultPin() = repository.clearVaultPin()

    fun resetData() = repository.resetData()

    class Factory(private val repository: GalleryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GalleryViewModel(repository) as T
        }
    }
}
