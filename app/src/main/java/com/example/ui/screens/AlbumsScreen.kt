package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.TouchApp
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GradientKey
import com.example.ui.GalleryUiState
import com.example.ui.GalleryViewModel
import com.example.ui.components.AlbumCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.IconButtonVariant
import com.example.ui.components.SectionHeader
import com.example.ui.components.XPhotosIconButton
import com.example.ui.theme.xPalette

private val emojiChoices = listOf("📸", "🌟", "🌊", "🏔️", "🎨", "📁", "🎞️", "🖤", "🎉", "🌇", "💜", "🧳")

private val folderGradients = listOf(
    GradientKey.ocean, GradientKey.violet, GradientKey.forest,
    GradientKey.ember, GradientKey.midnight, GradientKey.sunset
)

private data class SmartAlbumItem(
    val id: String,
    val title: String,
    val count: Int,
    val emoji: String,
    val uri: String?,
    val gradient: GradientKey
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlbumsScreen(
    uiState: GalleryUiState,
    viewModel: GalleryViewModel,
    onOpenAlbum: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.xPalette
    var createOpen by remember { mutableStateOf(false) }
    var albumName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf(emojiChoices.first()) }

    val smartAlbums = remember(uiState.photos, uiState.favorites) {
        listOf(
            SmartAlbumItem(
                id = "favorites",
                title = "Favorites",
                count = uiState.stats.favorites,
                emoji = "❤️",
                uri = uiState.favorites.firstOrNull()?.let { uiState.getPhoto(it)?.thumb }
                    ?: uiState.photos.firstOrNull()?.thumb,
                gradient = GradientKey.candy
            ),
            SmartAlbumItem(
                id = "recent",
                title = "Recently Added",
                count = uiState.photos.size.coerceAtMost(30),
                emoji = "🕒",
                uri = uiState.photos.firstOrNull()?.thumb,
                gradient = GradientKey.ocean
            ),
            SmartAlbumItem(
                id = "camera",
                title = "Camera Roll",
                count = uiState.photos.count { it.isCustom || it.bucketName.equals("Camera", ignoreCase = true) },
                emoji = "📷",
                uri = uiState.photos.firstOrNull { it.isCustom || it.bucketName.equals("Camera", ignoreCase = true) }?.thumb,
                gradient = GradientKey.graphite
            ),
            SmartAlbumItem(
                id = "all",
                title = "All Photos",
                count = uiState.photos.size,
                emoji = "🗂️",
                uri = uiState.photos.getOrNull(1)?.thumb ?: uiState.photos.firstOrNull()?.thumb,
                gradient = GradientKey.violet
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LOCAL STORAGE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = palette.textMuted
                )
                Text(
                    text = "Albums",
                    style = MaterialTheme.typography.displaySmall,
                    color = palette.text
                )
                Text(
                    text = "${uiState.deviceFolders.size} device folders · ${uiState.userAlbums.size} custom albums",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textMuted,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            XPhotosIconButton(
                icon = Icons.Rounded.Add,
                contentDescription = "Create album",
                variant = IconButtonVariant.solid,
                onClick = { createOpen = true },
                modifier = Modifier.testTag("create_album_button")
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    SectionHeader(
                        title = "Smart albums",
                        subtitle = "Automatically organized locally",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(smartAlbums, key = { it.id }) { smart ->
                            AlbumCard(
                                title = smart.title,
                                count = smart.count,
                                uri = smart.uri,
                                emoji = smart.emoji,
                                gradient = smart.gradient,
                                height = 112.dp,
                                onClick = { onOpenAlbum(smart.id) },
                                modifier = Modifier
                                    .width(148.dp)
                                    .testTag("smart_album_${smart.id}")
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(top = 28.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    SectionHeader(
                        title = "Device folders",
                        subtitle = "Camera, Screenshots, Downloads and more"
                    )
                    if (uiState.deviceFolders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(palette.surface)
                                .border(0.75.dp, palette.border, RoundedCornerShape(22.dp))
                        ) {
                            EmptyStateView(
                                icon = Icons.Rounded.Folder,
                                title = "No device folders yet",
                                message = "Grant storage permission in Library or capture/import photos to see local folders here."
                            )
                        }
                    } else {
                        uiState.deviceFolders.chunked(2).forEachIndexed { rowIdx, pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEachIndexed { colIdx, folder ->
                                    val idx = rowIdx * 2 + colIdx
                                    AlbumCard(
                                        title = folder.name,
                                        count = folder.count,
                                        uri = folder.coverUri,
                                        emoji = "📁",
                                        gradient = folderGradients[idx % folderGradients.size],
                                        height = 132.dp,
                                        onClick = { onOpenAlbum("folder:${folder.id}") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(top = 28.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    SectionHeader(
                        title = "My albums",
                        subtitle = "Custom local collections",
                        actionLabel = "New",
                        onAction = { createOpen = true }
                    )
                    if (uiState.userAlbums.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(palette.surface)
                                .border(0.75.dp, palette.border, RoundedCornerShape(22.dp))
                        ) {
                            EmptyStateView(
                                icon = Icons.Rounded.FolderSpecial,
                                title = "No custom albums yet",
                                message = "Group your local photos into custom albums without moving files on disk.",
                                actionLabel = "Create album",
                                onAction = { createOpen = true }
                            )
                        }
                    } else {
                        uiState.userAlbums.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { album ->
                                    val cover = album.photoIds.firstOrNull()?.let { uiState.getPhoto(it) }
                                    AlbumCard(
                                        title = album.title,
                                        count = album.photoIds.size,
                                        uri = cover?.thumb,
                                        emoji = album.emoji,
                                        height = 140.dp,
                                        onClick = { onOpenAlbum(album.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 24.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(palette.surface)
                        .border(0.75.dp, palette.border, RoundedCornerShape(22.dp))
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(palette.primarySoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TouchApp,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Long-press any photo",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.text
                        )
                        Text(
                            text = "Select multiple shots in Library to share, hide in your Private Vault, or organize into an album.",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = palette.textMuted,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }
        }
    }

    if (createOpen) {
        ModalBottomSheet(
            onDismissRequest = { createOpen = false },
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
                    text = "Create album",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.text,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
                Text(
                    text = "ALBUM NAME",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = albumName,
                    onValueChange = { if (it.length <= 40) albumName = it },
                    placeholder = { Text("e.g. Family Trip") },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_album_name_input")
                )
                Text(
                    text = "COVER EMOJI",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojiChoices.forEach { choice ->
                        val active = choice == selectedEmoji
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (active) palette.primarySoft else palette.surfaceAlt)
                                .border(
                                    0.75.dp,
                                    if (active) palette.primary else palette.border,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedEmoji = choice },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = choice, fontSize = 20.sp)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.text)
                        .clickable {
                            val id = viewModel.createAlbum(albumName, selectedEmoji)
                            createOpen = false
                            albumName = ""
                            selectedEmoji = emojiChoices.first()
                            onOpenAlbum(id)
                        }
                        .padding(vertical = 15.dp)
                        .testTag("confirm_create_album"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Create album",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.bg
                    )
                }
            }
        }
    }
}
