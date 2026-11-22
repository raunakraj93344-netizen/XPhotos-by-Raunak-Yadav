package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.GridDensity
import com.example.ui.GalleryUiState
import com.example.ui.GalleryViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PhotoCard
import com.example.ui.components.SmartImage
import com.example.ui.components.XPhotosIconButton
import com.example.ui.theme.xPalette
import com.example.util.GalleryUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: String,
    uiState: GalleryUiState,
    viewModel: GalleryViewModel,
    onBack: () -> Unit,
    onOpenPhoto: (String, String) -> Unit,
    onOpenSlideshow: (String, String) -> Unit,
    onOpenLibraryTab: () -> Unit,
    onOpenAlbumsTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.xPalette
    val context = LocalContext.current
    val album = remember(albumId, uiState) { viewModel.resolveAlbum(albumId) }

    if (album == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(palette.bg)
                .statusBarsPadding()
        ) {
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                XPhotosIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Go back",
                    onClick = onBack
                )
            }
            EmptyStateView(
                icon = Icons.Rounded.PhotoLibrary,
                title = "Album not found",
                message = "This album may have been deleted or moved.",
                actionLabel = "Back to albums",
                onAction = onOpenAlbumsTab
            )
        }
        return
    }

    var menuOpen by remember { mutableStateOf(false) }
    var renameOpen by remember { mutableStateOf(false) }
    var deleteOpen by remember { mutableStateOf(false) }
    var pickerOpen by remember { mutableStateOf(false) }
    var pickerSelected by remember { mutableStateOf(setOf<String>()) }
    var newName by remember(album.title) { mutableStateOf(album.title) }

    val favoriteSet = remember(uiState.favorites) { uiState.favorites.toSet() }
    val albumIdSet = remember(album.photoIds) { album.photoIds.toSet() }
    val cover = album.photos.firstOrNull()

    fun openPicker() {
        pickerSelected = album.photoIds.toSet()
        pickerOpen = true
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
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                XPhotosIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Go back",
                    onClick = onBack
                )
                Text(
                    text = "${album.emoji} ${album.title}",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                XPhotosIconButton(
                    icon = Icons.Rounded.MoreHoriz,
                    contentDescription = "Album options",
                    onClick = { menuOpen = true }
                )
            }

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(columns),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 10.dp
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Hero
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(22.dp))
                        ) {
                            if (cover != null) {
                                SmartImage(
                                    uri = cover.thumb,
                                    contentDescription = album.title,
                                    cornerRadius = 22.dp,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.linearGradient(album.gradientColors))
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Black.copy(alpha = 0.1f),
                                                Color.Black.copy(alpha = 0.45f),
                                                Color.Black.copy(alpha = 0.9f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = album.emoji, fontSize = 22.sp)
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = album.title,
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${album.photos.size} ${if (album.photos.size == 1) "photo" else "photos"} · ${album.subtitle}",
                                    color = Color.White.copy(alpha = 0.78f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                        }

                        // Action row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(palette.surface)
                                    .border(0.75.dp, palette.border, CircleShape)
                                    .clickable {
                                        album.photos.firstOrNull()?.let { first ->
                                            onOpenSlideshow(first.id, album.id)
                                        }
                                    }
                                    .padding(vertical = 11.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = palette.text, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Slideshow", color = palette.text, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(palette.surface)
                                    .border(0.75.dp, palette.border, CircleShape)
                                    .clickable {
                                        if (album.photos.isNotEmpty()) {
                                            GalleryUtils.sharePhotos(
                                                context,
                                                album.photos.take(10),
                                                uiState.appNameLabel
                                            )
                                        }
                                    }
                                    .padding(vertical = 11.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, tint = palette.text, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Share", color = palette.text, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                            if (album.isUser) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(palette.text)
                                        .clickable { openPicker() }
                                        .padding(vertical = 11.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, tint = palette.bg, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("Add photos", color = palette.bg, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(palette.surface)
                                        .border(0.75.dp, palette.border, CircleShape)
                                        .clickable(onClick = onOpenLibraryTab)
                                        .padding(vertical = 11.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = palette.text, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("Library", color = palette.text, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Photos",
                                style = MaterialTheme.typography.headlineMedium,
                                color = palette.text
                            )
                            Text(
                                text = "${album.photos.size} items",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textMuted
                            )
                        }
                    }
                }

                if (album.photos.isEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        EmptyStateView(
                            icon = Icons.Rounded.CreateNewFolder,
                            title = "This album is empty",
                            message = if (album.isUser) {
                                "Pick photos from your library to start filling this album."
                            } else {
                                "Nothing here yet — favorite photos or add device captures to fill this up."
                            },
                            actionLabel = if (album.isUser) "Add photos" else "Browse library",
                            onAction = {
                                if (album.isUser) openPicker() else onOpenLibraryTab()
                            }
                        )
                    }
                } else {
                    items(album.photos, key = { it.id }) { photo ->
                        PhotoCard(
                            photo = photo,
                            onClick = { onOpenPhoto(photo.id, album.id) },
                            favorite = photo.id in favoriteSet,
                            showLabel = true
                        )
                    }
                }

                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        text = when {
                            album.isFolder -> "Device folder · stored offline"
                            album.isSmart -> "Smart album · updates automatically"
                            else -> "Custom album · saved locally"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = palette.textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp)
                    )
                }
            }
        }
    }

    // Menu sheet
    if (menuOpen) {
        ModalBottomSheet(
            onDismissRequest = { menuOpen = false },
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
                    text = "Album options",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.text,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                if (album.isUser) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                menuOpen = false
                                openPicker()
                            }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Rounded.CreateNewFolder, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(18.dp))
                        Text("Add or remove photos", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = palette.text)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                menuOpen = false
                                newName = album.title
                                renameOpen = true
                            }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(18.dp))
                        Text("Rename album", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = palette.text)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            menuOpen = false
                            album.photos.firstOrNull()?.let { first ->
                                onOpenSlideshow(first.id, album.id)
                            }
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(18.dp))
                    Text("Play slideshow", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = palette.text)
                }
                if (album.isUser) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                menuOpen = false
                                deleteOpen = true
                            }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null, tint = palette.danger, modifier = Modifier.size(18.dp))
                        Text("Delete album", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = palette.danger)
                    }
                }
            }
        }
    }

    // Rename sheet
    if (renameOpen) {
        ModalBottomSheet(
            onDismissRequest = { renameOpen = false },
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
                    text = "Rename album",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.text,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
                OutlinedTextField(
                    value = newName,
                    onValueChange = { if (it.length <= 40) newName = it },
                    placeholder = { Text("Album name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = palette.surfaceAlt,
                        unfocusedContainerColor = palette.surfaceAlt,
                        focusedBorderColor = palette.primary,
                        unfocusedBorderColor = palette.border,
                        focusedTextColor = palette.text,
                        unfocusedTextColor = palette.text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.text)
                        .clickable {
                            viewModel.renameAlbum(album.id, newName)
                            renameOpen = false
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save changes",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.bg
                    )
                }
            }
        }
    }

    // Delete sheet
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
                    text = "Delete album?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.text
                )
                Text(
                    text = "“${album.title}” will be deleted. The photos inside stay safe in your library.",
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
                                viewModel.deleteAlbum(album.id)
                                deleteOpen = false
                                onBack()
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

    // Full-screen photo picker dialog for adding/removing photos in user album
    if (pickerOpen) {
        Dialog(
            onDismissRequest = { pickerOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.bg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    XPhotosIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = "Close picker",
                        onClick = { pickerOpen = false }
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Add photos",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.text
                        )
                        Text(
                            text = "${pickerSelected.size} selected · ${album.title}",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textMuted
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(palette.text)
                            .clickable {
                                val toAdd = pickerSelected.filter { it !in albumIdSet }
                                val toRemove = album.photoIds.filter { it !in pickerSelected }
                                if (toAdd.isNotEmpty()) viewModel.addPhotosToAlbum(album.id, toAdd)
                                if (toRemove.isNotEmpty()) viewModel.removePhotosFromAlbum(album.id, toRemove)
                                pickerOpen = false
                            }
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = "Done",
                            color = palette.bg,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "Tap to add or remove. Photos already in this album are pre-selected.",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textMuted,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(uiState.photos, key = { it.id }) { photo ->
                        PhotoCard(
                            photo = photo,
                            onClick = {
                                pickerSelected = if (photo.id in pickerSelected) {
                                    pickerSelected - photo.id
                                } else {
                                    pickerSelected + photo.id
                                }
                            },
                            aspectRatio = 1f,
                            selectionMode = true,
                            selected = photo.id in pickerSelected,
                            favorite = photo.id in favoriteSet,
                            showLabel = false,
                            cornerRadius = 12.dp
                        )
                    }
                }
            }
        }
    }
}
