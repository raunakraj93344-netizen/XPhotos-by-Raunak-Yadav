package com.example.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
enum class GradientKey {
    violet,
    sunset,
    ocean,
    forest,
    ember,
    midnight,
    candy,
    graphite
}

@Serializable
enum class ThemeMode(val label: String) {
    system("System"),
    light("Light"),
    dark("Dark")
}

@Serializable
enum class GridDensity(val label: String) {
    comfortable("Comfortable"),
    compact("Compact")
}

@Serializable
enum class SortOption(val label: String, val description: String) {
    newest("Newest first", "Most recent device photos at the top"),
    oldest("Oldest first", "Earliest photos shown first"),
    az("Name A–Z", "Alphabetical by file or photo title"),
    largest("Largest first", "Sorted by file size"),
    folder("By folder", "Grouped by device folder")
}

enum class ViewMode {
    masonry,
    grid
}

@Serializable
enum class AppNameOption(
    val displayName: String,
    val aliasClassName: String,
    val tagline: String
) {
    XPhotos("XPhotos", "com.example.alias.XPhotosAlias", "Default signature branding"),
    Gallery("Gallery", "com.example.alias.GalleryAlias", "Clean classic gallery label"),
    Photos("Photos", "com.example.alias.PhotosAlias", "Minimalist OS-style label"),
    PixelVault("PixelVault", "com.example.alias.PixelVaultAlias", "Private vault aesthetic"),
    FrameBox("FrameBox", "com.example.alias.FrameBoxAlias", "Editorial frame studio")
}

@Serializable
enum class AccentPreset(
    val label: String,
    val darkColorHex: Long,
    val lightColorHex: Long
) {
    Violet("Electric Violet", 0xFF8B6BFF, 0xFF6C4CFF),
    Emerald("Emerald Mint", 0xFF3DDC97, 0xFF12A56D),
    Rose("Crimson Rose", 0xFFFF5C8A, 0xFFE6396B),
    Amber("Amber Gold", 0xFFFFB020, 0xFFD98200),
    Cobalt("Cobalt Blue", 0xFF4D94FF, 0xFF2563EB),
    Cyan("Arctic Cyan", 0xFF22D3EE, 0xFF0891B2)
}

@Serializable
data class Photo(
    val id: String,
    val mediaStoreId: Long? = null,
    val uri: String,
    val thumb: String,
    val width: Int,
    val height: Int,
    val title: String,
    val bucketId: String = "camera",
    val bucketName: String = "Camera",
    val relativePath: String = "App Storage",
    val mimeType: String = "image/jpeg",
    val sizeBytes: Long = 0L,
    val epochMillis: Long = 0L,
    val isCustom: Boolean = false
)

data class ExifDetails(
    val camera: String = "Unknown camera",
    val lens: String = "Standard lens",
    val iso: String = "—",
    val aperture: String = "—",
    val shutter: String = "—",
    val focalLength: String = "—",
    val rawTags: List<Pair<String, String>> = emptyList()
)

data class DeviceFolder(
    val id: String,
    val name: String,
    val count: Int,
    val coverUri: String?,
    val totalBytes: Long
)

@Serializable
data class UserAlbum(
    val id: String,
    val title: String,
    val emoji: String,
    val gradient: GradientKey = GradientKey.violet,
    val photoIds: List<String> = emptyList(),
    val createdAt: String
)

@Serializable
data class Settings(
    val themeMode: ThemeMode = ThemeMode.dark,
    val density: GridDensity = GridDensity.comfortable,
    val sort: SortOption = SortOption.newest,
    val autoplay: Boolean = true,
    val showMeta: Boolean = true
)

@Serializable
data class DeveloperSettings(
    val unlocked: Boolean = false,
    val appName: AppNameOption = AppNameOption.XPhotos,
    val showFpsRamHud: Boolean = false,
    val showRawExifAndPath: Boolean = false,
    val accentPreset: AccentPreset = AccentPreset.Violet,
    val pureAmoledDark: Boolean = false,
    val animationSpeedScale: Float = 1.0f,
    val slideshowIntervalMs: Int = 4600,
    val slideshowShuffle: Boolean = false
)

@Serializable
data class PersistedState(
    val favorites: List<String> = emptyList(),
    val userAlbums: List<UserAlbum> = emptyList(),
    val customPhotos: List<Photo> = emptyList(),
    val hiddenIds: List<String> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val settings: Settings = Settings(),
    val developerSettings: DeveloperSettings = DeveloperSettings(),
    val vaultPinHash: String? = null
)

data class FolderStorageStat(
    val folderName: String,
    val count: Int,
    val bytes: Long
)

data class GalleryStats(
    val totalPhotos: Int = 0,
    val favorites: Int = 0,
    val albums: Int = 0,
    val hiddenCount: Int = 0,
    val storageBytes: Long = 0L,
    val folderStats: List<FolderStorageStat> = emptyList()
)

data class ResolvedAlbum(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val gradientColors: List<Color>,
    val photos: List<Photo>,
    val photoIds: List<String>,
    val isUser: Boolean,
    val isSmart: Boolean,
    val isFolder: Boolean = false
)

enum class WallpaperTarget(val label: String) {
    Home("Home screen"),
    Lock("Lock screen"),
    Both("Home & Lock")
}

data class DeviceDiagnostics(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkInt: Int,
    val supportedAbis: String,
    val screenMetrics: String,
    val packageName: String,
    val versionName: String,
    val availableStorageBytes: Long,
    val totalStorageBytes: Long
)
