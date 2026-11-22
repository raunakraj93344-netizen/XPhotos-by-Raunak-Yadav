package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.ViewQuilt
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeleteOutcome
import com.example.model.GridDensity
import com.example.model.Photo
import com.example.model.SortOption
import com.example.model.ViewMode
import com.example.ui.GalleryUiState
import com.example.ui.GalleryViewModel
import com.example.ui.components.BrandHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.IconButtonVariant
import com.example.ui.components.MemoryCard
import com.example.ui.components.PhotoCard
import com.example.ui.components.StatTile
import com.example.ui.components.XPhotosChip
import com.example.ui.components.XPhotosIconButton
import com.example.ui.theme.xPalette
import com.example.util.GalleryUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    uiState: GalleryUiState,
    viewModel: GalleryViewModel,
    onOpenPhoto: (String) -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenSlideshow: (String?) -> Unit,
    onOpenCamera: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAlbumsTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.xPalette
    val context = LocalContext.current

    var filterKey by rememberSaveable { mutableStateOf("all") }
    var viewMode by rememberSaveable { mutableStateOf(ViewMode.masonry) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var sortOpen by remember { mutableStateOf(false) }
    var albumOpen by remember { mutableStateOf(false) }
    var deleteOpen by remember { mutableStateOf(false) }

    val mediaPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshDevicePhotos()
    }

    val deleteSenderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onDeviceDeleteConfirmed(result.resultCode == Activity.RESULT_OK)
    }

    val multiPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(20)
    ) { uris: List<Uri> ->
        uris.forEachIndexed { idx, uri ->
            val localUri = runCatching {
                val dir = File(context.filesDir, "photos").apply { mkdirs() }
                val dest = File(dir, "import_${System.currentTimeMillis()}_$idx.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { out -> input.copyTo(out) }
                }
                Uri.fromFile(dest).toString()
            }.getOrDefault(uri.toString())
            viewModel.addCapturedPhoto(uri = localUri, title = "Imported Photo")
        }
    }

    val favoriteSet = remember(uiState.favorites) { uiState.favorites.toSet() }

    val filtered = remember(uiState.photos, filterKey, favoriteSet, uiState.settings.sort) {
        val base = when {
            filterKey == "all" -> uiState.photos
            filterKey == "favorites" -> uiState.photos.filter { it.id in favoriteSet }
            filterKey == "recent" -> uiState.photos.take(30)
            filterKey.startsWith("folder:") -> {
                val bucketId = filterKey.removePrefix("folder:")
                uiState.photos.filter { it.bucketId == bucketId }
            }
            else -> uiState.photos
        }
        when (uiState.settings.sort) {
            SortOption.newest -> base.sortedByDescending { it.epochMillis }
            SortOption.oldest -> base.sortedBy { it.epochMillis }
            SortOption.az -> base.sortedBy { it.title.lowercase() }
            SortOption.largest -> base.sortedByDescending { GalleryUtils.estimateSize(it) }
            SortOption.folder -> base.sortedBy { it.bucketName.lowercase() }
        }
    }

    val filterChips = remember(uiState.deviceFolders) {
        listOf("all" to "All", "favorites" to "Favorites", "recent" to "Recent") +
            uiState.deviceFolders.take(8).map { "folder:${it.id}" to it.name }
    }

    val memoryPhoto = uiState.photos.firstOrNull()

    fun enterSelection(photo: Photo) {
        selectionMode = true
        selectedIds = setOf(photo.id)
    }

    fun toggleSelected(photo: Photo) {
        selectedIds = if (photo.id in selectedIds) selectedIds - photo.id else selectedIds + photo.id
    }

    fun exitSelection() {
        selectionMode = false
        selectedIds = emptySet()
    }

    fun handlePhotoClick(photo: Photo) {
        if (selectionMode) toggleSelected(photo) else onOpenPhoto(photo.id)
    }

    if (!uiState.hydrated) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(palette.bg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = palette.primary)
        }
        return
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bg)
    ) {
        val baseColumns = when {
            maxWidth >= 760.dp -> 4
            maxWidth >= 520.dp -> 3
            else -> 2
        }
        val columns = if (uiState.settings.density == GridDensity.compact) {
            (baseColumns + 1).coerceAtMost(4)
        } else baseColumns

        Column(modifier = Modifier.fillMaxSize()) {
            if (selectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    XPhotosIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = "Exit selection",
                        onClick = ::exitSelection,
                        variant = IconButtonVariant.surface,
                        modifier = Modifier.testTag("exit_selection_button")
                    )
                    Text(
                        text = "${selectedIds.size}",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        color = palette.text,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(CircleShape)
                            .background(palette.surfaceAlt)
                            .clickable {
                                selectedIds = if (selectedIds.size == filtered.size) emptySet()
                                else filtered.map { it.id }.toSet()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("select_all_toggle")
                    ) {
                        Text(
                            text = if (selectedIds.size == filtered.size && filtered.isNotEmpty()) "None" else "All",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.textSecondary
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    XPhotosIconButton(
                        icon = Icons.Rounded.Share,
                        contentDescription = "Share selected",
                        onClick = {
                            val chosen = uiState.photos.filter { it.id in selectedIds }
                            if (GalleryUtils.sharePhotos(context, chosen, uiState.appNameLabel)) {
                                exitSelection()
                            }
                        },
                        variant = IconButtonVariant.surface,
                        size = 38.dp
                    )
                    Spacer(Modifier.size(5.dp))
                    XPhotosIconButton(
                        icon = Icons.Rounded.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        onClick = {
                            val allFav = selectedIds.isNotEmpty() && selectedIds.all { it in favoriteSet }
                            viewModel.setFavoriteForMany(selectedIds.toList(), !allFav)
                            exitSelection()
                        },
                        variant = IconButtonVariant.surface,
                        size = 38.dp
                    )
                    Spacer(Modifier.size(5.dp))
                    XPhotosIconButton(
                        icon = Icons.Rounded.CreateNewFolder,
                        contentDescription = "Add to album",
                        onClick = { if (selectedIds.isNotEmpty()) albumOpen = true },
                        variant = IconButtonVariant.surface,
                        size = 38.dp
                    )
                    Spacer(Modifier.size(5.dp))
                    XPhotosIconButton(
                        icon = Icons.Rounded.VisibilityOff,
                        contentDescription = "Hide selected",
                        onClick = {
                            if (selectedIds.isNotEmpty()) {
                                viewModel.hidePhotos(selectedIds.toList())
                                exitSelection()
                            }
                        },
                        variant = IconButtonVariant.surface,
                        size = 38.dp
                    )
                    Spacer(Modifier.size(5.dp))
                    XPhotosIconButton(
                        icon = Icons.Rounded.Delete,
                        contentDescription = "Delete selected",
                        tint = palette.danger,
                        onClick = { if (selectedIds.isNotEmpty()) deleteOpen = true },
                        variant = IconButtonVariant.surface,
                        size = 38.dp
                    )
                }
                HorizontalDivider(thickness = 0.75.dp, color = palette.border)
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BrandHeader(appName = uiState.appNameLabel)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        XPhotosIconButton(
                            icon = Icons.Rounded.AddPhotoAlternate,
                            contentDescription = "Import photos",
                            onClick = {
                                multiPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.testTag("library_import_button")
                        )
                        XPhotosIconButton(
                            icon = Icons.Rounded.PlayArrow,
                            contentDescription = "Play slideshow",
                            onClick = { onOpenSlideshow(uiState.photos.firstOrNull()?.id) },
                            modifier = Modifier.testTag("library_slideshow_button")
                        )
                    }
                }
            }

            val headerBlock: @Composable () -> Unit = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!uiState.hasMediaPermission) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(palette.surface)
                                .border(0.75.dp, palette.border, RoundedCornerShape(22.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(palette.primarySoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.FolderOpen,
                                        contentDescription = null,
                                        tint = palette.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Access device photos offline",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = palette.text
                                    )
                                    Text(
                                        text = "Grant photo permission to browse Camera, Screenshots, and device folders with zero internet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = palette.textMuted,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(palette.primary)
                                        .clickable { mediaPermissionLauncher.launch(mediaPermissions) }
                                        .padding(vertical = 12.dp)
                                        .testTag("grant_media_permission_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Allow access",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(palette.surfaceAlt)
                                        .border(0.75.dp, palette.border, RoundedCornerShape(14.dp))
                                        .clickable {
                                            multiPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Pick photos",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = palette.text
                                    )
                                }
                            }
                        }
                    } else if (memoryPhoto != null) {
                        MemoryCard(
                            uri = memoryPhoto.thumb,
                            count = uiState.stats.totalPhotos,
                            onClick = { onOpenAlbum("recent") },
                            onReplay = { onOpenSlideshow(memoryPhoto.id) }
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.surfaceAlt)
                            .border(0.75.dp, palette.border, RoundedCornerShape(16.dp))
                            .clickable(onClick = onOpenSearch)
                            .padding(horizontal = 14.dp)
                            .testTag("library_search_bar"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = palette.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Search local photos, folders, dates…",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                            color = palette.textMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            icon = Icons.Rounded.PhotoLibrary,
                            label = "Photos",
                            value = "${uiState.stats.totalPhotos}",
                            tint = palette.primary,
                            onClick = { onOpenAlbum("all") },
                            modifier = Modifier.weight(1f).testTag("stat_photos")
                        )
                        StatTile(
                            icon = Icons.Rounded.Favorite,
                            label = "Favorites",
                            value = "${uiState.stats.favorites}",
                            tint = palette.accent,
                            onClick = { onOpenAlbum("favorites") },
                            modifier = Modifier.weight(1f).testTag("stat_favorites")
                        )
                        StatTile(
                            icon = Icons.Rounded.Layers,
                            label = "Folders",
                            value = "${uiState.deviceFolders.size}",
                            tint = palette.success,
                            onClick = onOpenAlbumsTab,
                            modifier = Modifier.weight(1f).testTag("stat_albums")
                        )
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filterChips, key = { it.first }) { (key, label) ->
                            XPhotosChip(
                                label = label,
                                active = filterKey == key,
                                compact = true,
                                onClick = { filterKey = key },
                                modifier = Modifier.testTag("filter_chip_$key")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = when {
                                    filterKey == "all" -> "On this device"
                                    filterKey == "favorites" -> "Favorites"
                                    filterKey == "recent" -> "Recent"
                                    filterKey.startsWith("folder:") -> {
                                        val bId = filterKey.removePrefix("folder:")
                                        uiState.deviceFolders.find { it.id == bId }?.name ?: "Folder"
                                    }
                                    else -> filterKey
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                color = palette.text
                            )
                            Text(
                                text = "${filtered.size} ${if (filtered.size == 1) "photo" else "photos"}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                color = palette.textMuted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(palette.surfaceAlt)
                                    .border(0.75.dp, palette.border, CircleShape)
                                    .clickable { sortOpen = true }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                    .testTag("sort_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapVert,
                                    contentDescription = null,
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = uiState.settings.sort.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.textSecondary
                                )
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = palette.textMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(palette.surfaceAlt)
                                    .border(0.75.dp, palette.border, CircleShape)
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 30.dp, height = 26.dp)
                                        .clip(CircleShape)
                                        .background(if (viewMode == ViewMode.masonry) palette.surface else Color.Transparent)
                                        .clickable { viewMode = ViewMode.masonry }
                                        .testTag("view_mode_masonry"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ViewQuilt,
                                        contentDescription = "Masonry view",
                                        tint = if (viewMode == ViewMode.masonry) palette.text else palette.textMuted,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(width = 30.dp, height = 26.dp)
                                        .clip(CircleShape)
                                        .background(if (viewMode == ViewMode.grid) palette.surface else Color.Transparent)
                                        .clickable { viewMode = ViewMode.grid }
                                        .testTag("view_mode_grid"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GridView,
                                        contentDescription = "Grid view",
                                        tint = if (viewMode == ViewMode.grid) palette.text else palette.textMuted,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val emptyBlock: @Composable () -> Unit = {
                EmptyStateView(
                    icon = if (filterKey == "favorites") Icons.Rounded.Favorite else Icons.Rounded.PhotoLibrary,
                    title = if (filterKey == "favorites") "No favorites yet" else "No local photos found",
                    message = if (filterKey == "favorites") {
                        "Tap the heart on any photo to keep it in Favorites."
                    } else if (!uiState.hasMediaPermission) {
                        "Allow photo access above, import from Photo Picker, or capture a new shot offline."
                    } else {
                        "Capture a photo or import images from your device to see them here."
                    },
                    actionLabel = "Open camera",
                    onAction = onOpenCamera
                )
            }

            val footerBlock: @Composable () -> Unit = {
                Text(
                    text = "${uiState.stats.totalPhotos} photos · ${uiState.deviceFolders.size} folders · 100% offline on device",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = palette.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )
            }

            if (viewMode == ViewMode.masonry) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(columns),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp
                ) {
                    item(span = StaggeredGridItemSpan.FullLine) { headerBlock() }
                    if (filtered.isEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) { emptyBlock() }
                    } else {
                        items(filtered, key = { it.id }) { photo ->
                            PhotoCard(
                                photo = photo,
                                onClick = { handlePhotoClick(photo) },
                                onLongClick = { enterSelection(photo) },
                                selectionMode = selectionMode,
                                selected = photo.id in selectedIds,
                                favorite = photo.id in favoriteSet,
                                showLabel = true
                            )
                        }
                    }
                    item(span = StaggeredGridItemSpan.FullLine) { footerBlock() }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) { headerBlock() }
                    if (filtered.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) { emptyBlock() }
                    } else {
                        items(filtered, key = { it.id }) { photo ->
                            PhotoCard(
                                photo = photo,
                                onClick = { handlePhotoClick(photo) },
                                onLongClick = { enterSelection(photo) },
                                aspectRatio = 1f,
                                selectionMode = selectionMode,
                                selected = photo.id in selectedIds,
                                favorite = photo.id in favoriteSet,
                                showLabel = false,
                                cornerRadius = 12.dp
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) { footerBlock() }
                }
            }
        }

        if (!selectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
                    .size(56.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(palette.primary, palette.accent)))
                    .clickable(onClick = onOpenCamera)
                    .testTag("camera_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = "Open camera",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (sortOpen) {
        ModalBottomSheet(
            onDismissRequest = { sortOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = palette.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Sort photos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.text,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                SortOption.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setSort(option)
                                sortOpen = false
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = palette.text
                            )
                            Text(
                                text = option.description,
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textMuted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        if (uiState.settings.sort == option) {
                            Icon(Icons.Rounded.Check, contentDescription = "Selected", tint = palette.primary)
                        }
                    }
                }
            }
        }
    }

    if (albumOpen) {
        ModalBottomSheet(
            onDismissRequest = { albumOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = palette.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Add to album",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.text,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (uiState.userAlbums.isEmpty()) {
                    Text(
                        text = "No custom albums yet. Create one in the Albums tab.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textMuted,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.text)
                            .clickable {
                                albumOpen = false
                                exitSelection()
                                onOpenAlbumsTab()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Go to albums",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.bg
                        )
                    }
                } else {
                    uiState.userAlbums.forEach { album ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.addPhotosToAlbum(album.id, selectedIds.toList())
                                    albumOpen = false
                                    exitSelection()
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = album.emoji, fontSize = 22.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = album.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = palette.text
                                )
                                Text(
                                    text = "${album.photoIds.size} photos",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.textMuted
                                )
                            }
                            Icon(Icons.Rounded.CreateNewFolder, contentDescription = null, tint = palette.textMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (deleteOpen) {
        ModalBottomSheet(
            onDismissRequest = { deleteOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = palette.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Delete ${selectedIds.size} photos?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.text
                )
                Text(
                    text = "Selected photos will be permanently deleted from your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textMuted,
                    modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(0.75.dp, palette.border, RoundedCornerShape(16.dp))
                            .clickable { deleteOpen = false }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = palette.text)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.danger)
                            .clickable {
                                val chosen = uiState.photos.filter { it.id in selectedIds }
                                deleteOpen = false
                                exitSelection()
                                viewModel.requestDeletePhotos(chosen) { outcome ->
                                    if (outcome is DeleteOutcome.RequiresConfirmation) {
                                        deleteSenderLauncher.launch(
                                            IntentSenderRequest.Builder(outcome.intentSender).build()
                                        )
                                    }
                                }
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Delete", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }
            }
        }
    }
}
