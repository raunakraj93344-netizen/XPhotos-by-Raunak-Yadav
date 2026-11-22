package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallMade
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GradientKey
import com.example.model.GridDensity
import com.example.ui.GalleryUiState
import com.example.ui.GalleryViewModel
import com.example.ui.components.AlbumCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PhotoCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.XPhotosChip
import com.example.ui.theme.XPhotosGradients
import com.example.ui.theme.xPalette
import com.example.util.GalleryUtils

private val quickLocalKeywords = listOf(
    "Camera", "Screenshots", "Downloads", "Pictures", "jpeg", "png", "2026", "2025"
)

private val folderTints = listOf(
    GradientKey.forest, GradientKey.ocean, GradientKey.midnight,
    GradientKey.ember, GradientKey.sunset, GradientKey.candy,
    GradientKey.violet, GradientKey.graphite
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    initialQuery: String?,
    uiState: GalleryUiState,
    viewModel: GalleryViewModel,
    onOpenPhoto: (String) -> Unit,
    onOpenAlbum: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.xPalette
    val keyboard = LocalSoftwareKeyboardController.current

    var query by rememberSaveable { mutableStateOf(initialQuery.orEmpty()) }
    var folderFilter by rememberSaveable { mutableStateOf("all") }

    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank()) {
            query = initialQuery
            folderFilter = "all"
            viewModel.recordSearch(initialQuery)
        }
    }

    val baseResults = remember(uiState.photos, query) {
        GalleryUtils.searchPhotos(uiState.photos, query)
    }

    val results = remember(baseResults, folderFilter) {
        if (folderFilter == "all") baseResults
        else baseResults.filter { it.bucketId == folderFilter }
    }

    val availableFolders = remember(baseResults) {
        baseResults
            .groupBy { it.bucketId to it.bucketName }
            .keys
            .toList()
    }

    val active = query.trim().isNotEmpty()

    fun applyQuery(next: String) {
        query = next
        folderFilter = "all"
        viewModel.recordSearch(next)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "OFFLINE SEARCH",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = palette.textMuted
                )
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.displaySmall,
                    color = palette.text,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surface)
                        .border(0.75.dp, palette.border, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = palette.textMuted,
                        modifier = Modifier.size(19.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = "File name, folder, date, format…",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                color = palette.textMuted
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.sp,
                                color = palette.text
                            ),
                            cursorBrush = SolidColor(palette.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    viewModel.recordSearch(query)
                                    keyboard?.hide()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_input")
                        )
                    }
                    if (query.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(palette.surfaceAlt)
                                .clickable {
                                    query = ""
                                    folderFilter = "all"
                                }
                                .testTag("clear_search_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = palette.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            if (active) {
                val favoriteSet = remember(uiState.favorites) { uiState.favorites.toSet() }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${results.size} ${if (results.size == 1) "result" else "results"}",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
                                        color = palette.text
                                    )
                                    Text(
                                        text = "for “${query.trim()}”",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                        color = palette.textMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(palette.primarySoft)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Storage,
                                        contentDescription = null,
                                        tint = palette.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Local index",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = palette.primary
                                    )
                                }
                            }

                            if (availableFolders.size > 1) {
                                LazyRow(
                                    contentPadding = PaddingValues(top = 12.dp, bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        XPhotosChip(
                                            label = "All",
                                            compact = true,
                                            active = folderFilter == "all",
                                            onClick = { folderFilter = "all" }
                                        )
                                    }
                                    items(availableFolders, key = { it.first }) { (bId, bName) ->
                                        XPhotosChip(
                                            label = bName,
                                            compact = true,
                                            active = folderFilter == bId,
                                            onClick = { folderFilter = bId }
                                        )
                                    }
                                }
                            } else {
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }

                    if (results.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyStateView(
                                icon = Icons.Rounded.Search,
                                title = "No matching local photos",
                                message = "Try a folder name (Camera, Screenshots), month, year, or file name.",
                                actionLabel = "Clear search",
                                onAction = { query = "" }
                            )
                        }
                    } else {
                        items(results, key = { it.id }) { photo ->
                            PhotoCard(
                                photo = photo,
                                onClick = { onOpenPhoto(photo.id) },
                                aspectRatio = 1f,
                                favorite = photo.id in favoriteSet,
                                showLabel = true,
                                cornerRadius = 12.dp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp)
                ) {
                    if (uiState.recentSearches.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                SectionHeader(
                                    title = "Recent",
                                    actionLabel = "Clear",
                                    onAction = { viewModel.clearRecentSearches() }
                                )
                                uiState.recentSearches.forEach { term ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { applyQuery(term) }
                                            .padding(vertical = 11.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(palette.surfaceAlt),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.History,
                                                contentDescription = null,
                                                tint = palette.textMuted,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Text(
                                            text = term,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = palette.text,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.CallMade,
                                            contentDescription = null,
                                            tint = palette.textMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    HorizontalDivider(thickness = 0.75.dp, color = palette.border)
                                }
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(top = 20.dp)) {
                            SectionHeader(
                                title = "Quick filters",
                                subtitle = "Search folders, dates, and formats"
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                quickLocalKeywords.forEach { tag ->
                                    XPhotosChip(
                                        label = tag,
                                        onClick = { applyQuery(tag) }
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.deviceFolders.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(top = 28.dp)) {
                                SectionHeader(
                                    title = "Device folders",
                                    subtitle = "${uiState.deviceFolders.size} local folders"
                                )
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
                                                gradient = folderTints[idx % folderTints.size],
                                                height = 108.dp,
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 18.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Brush.linearGradient(XPhotosGradients.searchTip))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Folder,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD479),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Fast offline indexing",
                                    color = Color.White,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Combine folder and date terms like “Camera 2026” or “Screenshots”. Everything stays on your device.",
                                    color = Color.White.copy(alpha = 0.72f),
                                    fontSize = 11.5.sp,
                                    lineHeight = 17.sp,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
