package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeleteOutcome
import com.example.model.ExifDetails
import com.example.model.WallpaperTarget
import com.example.ui.GalleryUiState
import com.example.ui.GalleryViewModel
import com.example.ui.components.IconButtonVariant
import com.example.ui.components.SmartImage
import com.example.ui.components.XPhotosIconButton
import com.example.util.GalleryUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    photoId: String,
    albumId: String? = null,
    fromVault: Boolean = false,
    uiState: GalleryUiState,
    viewModel: GalleryViewModel,
    onBack: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    onOpenSlideshow: (String) -> Unit,
    onOpenAlbumsTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val photos = remember(uiState, albumId, fromVault) {
        when {
            fromVault -> uiState.hiddenPhotos
            !albumId.isNullOrBlank() -> viewModel.resolveAlbum(albumId)?.photos ?: uiState.photos
            else -> uiState.photos
        }
    }

    val deleteSenderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val approved = result.resultCode == Activity.RESULT_OK
        viewModel.onDeviceDeleteConfirmed(approved)
        if (approved && photos.size <= 1) {
            onBack()
        }
    }

    if (photos.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Photo not found", color = Color.White)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Go back",
                    color = Color(0xFF8B6BFF),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onBack)
                )
            }
        }
        return
    }

    val initialIndex = remember(photoId, photos) {
        photos.indexOfFirst { it.id == photoId }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { photos.size })

    LaunchedEffect(photoId, photos.size) {
        val target = photos.indexOfFirst { it.id == photoId }
        if (target >= 0 && target != pagerState.currentPage) {
            pagerState.scrollToPage(target)
        }
    }

    var zoomed by remember(pagerState.currentPage) { mutableStateOf(false) }
    var infoOpen by remember { mutableStateOf(false) }
    var albumOpen by remember { mutableStateOf(false) }
    var wallpaperOpen by remember { mutableStateOf(false) }
    var deleteOpen by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var exif by remember { mutableStateOf(ExifDetails()) }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2000)
            toastMessage = null
        }
    }

    val currentPhoto = photos.getOrElse(pagerState.currentPage) { photos.first() }
    val isFavorite = uiState.isFavorite(currentPhoto.id)
    val scale by animateFloatAsState(targetValue = if (zoomed) 2f else 1f, label = "zoom")

    LaunchedEffect(currentPhoto.id, infoOpen) {
        if (infoOpen) {
            exif = viewModel.readExif(currentPhoto)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = photos[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(page) {
                        detectTapGestures(
                            onDoubleTap = { zoomed = !zoomed }
                        )
                    }
            ) {
                SmartImage(
                    uri = item.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(32.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                )
                SmartImage(
                    uri = item.uri,
                    contentDescription = item.title,
                    contentScale = ContentScale.Fit,
                    transparentBackground = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (page == pagerState.currentPage) {
                                scaleX = scale
                                scaleY = scale
                            }
                        }
                )
            }
        }

        // Top gradient bar
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.72f), Color.Transparent)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                XPhotosIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Close viewer",
                    variant = IconButtonVariant.ghost,
                    onClick = onBack,
                    modifier = Modifier.testTag("viewer_back_button")
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${photos.size}",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    XPhotosIconButton(
                        icon = Icons.Rounded.Info,
                        contentDescription = "Photo info",
                        variant = IconButtonVariant.ghost,
                        onClick = { infoOpen = true },
                        modifier = Modifier.testTag("viewer_info_button")
                    )
                    XPhotosIconButton(
                        icon = Icons.Rounded.Share,
                        contentDescription = "Share photo",
                        variant = IconButtonVariant.ghost,
                        onClick = {
                            if (!GalleryUtils.sharePhoto(context, currentPhoto, uiState.appNameLabel)) {
                                toastMessage = "Unable to open share sheet for this photo"
                            }
                        },
                        modifier = Modifier.testTag("viewer_share_button")
                    )
                }
            }
        }

        // Bottom gradient overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.82f),
                            Color.Black.copy(alpha = 0.96f)
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentPhoto.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (uiState.settings.showMeta) {
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Folder,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = currentPhoto.bucketName,
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(text = "·", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = GalleryUtils.formatDate(currentPhoto.epochMillis),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${currentPhoto.width} × ${currentPhoto.height} · ${
                                GalleryUtils.formatBytes(GalleryUtils.estimateSize(currentPhoto))
                            }",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFavorite) Color(0xE6FF5C8A)
                                else Color.White.copy(alpha = 0.16f)
                            )
                            .border(0.75.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                            .clickable { viewModel.toggleFavorite(currentPhoto.id) }
                            .testTag("viewer_favorite_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = Color.White,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    if (!fromVault) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                                .border(0.75.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                                .clickable { onOpenSlideshow(currentPhoto.id) }
                                .testTag("viewer_slideshow_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play slideshow",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Quick action row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!fromVault) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(0.75.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                            .clickable { albumOpen = true }
                            .padding(vertical = 10.dp)
                            .testTag("viewer_album_button"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CreateNewFolder, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Album", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(0.75.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                        .clickable { wallpaperOpen = true }
                        .padding(vertical = 10.dp)
                        .testTag("viewer_wallpaper_button"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Wallpaper, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Wallpaper", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(0.75.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                        .clickable {
                            if (fromVault) {
                                viewModel.unhidePhotos(listOf(currentPhoto.id))
                                toastMessage = "Restored to Library"
                            } else {
                                viewModel.hidePhotos(listOf(currentPhoto.id))
                                toastMessage = "Moved to Private Vault"
                            }
                            if (photos.size <= 1) onBack()
                        }
                        .padding(vertical = 10.dp)
                        .testTag("viewer_hide_button"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (fromVault) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (fromVault) "Unhide" else "Hide",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(0.75.dp, Color(0x80FF5D5D), CircleShape)
                        .clickable { deleteOpen = true }
                        .padding(vertical = 10.dp)
                        .testTag("viewer_delete_button"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color(0xFFFF8080), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", color = Color(0xFFFF8080), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Toast pill
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 60.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xF514141D))
                    .border(0.75.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Text(
                    text = toastMessage.orEmpty(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Photo Details & EXIF Sheet
    if (infoOpen) {
        val related = remember(photos, currentPhoto) {
            photos.filter { it.bucketId == currentPhoto.bucketId && it.id != currentPhoto.id }.take(10)
        }
        val exifRows = listOf(
            "Camera" to exif.camera,
            "Lens" to exif.lens,
            "ISO" to exif.iso,
            "Aperture" to exif.aperture,
            "Shutter" to exif.shutter,
            "Focal length" to exif.focalLength,
            "Resolution" to "${currentPhoto.width} × ${currentPhoto.height}",
            "File size" to GalleryUtils.formatBytes(GalleryUtils.estimateSize(currentPhoto)),
            "Folder" to currentPhoto.bucketName,
            "Format" to currentPhoto.mimeType
        )

        ModalBottomSheet(
            onDismissRequest = { infoOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF14141D)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Photo details",
                    color = Color(0xFFF6F6FA),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SmartImage(
                        uri = currentPhoto.thumb,
                        contentDescription = currentPhoto.title,
                        cornerRadius = 12.dp,
                        modifier = Modifier.size(76.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentPhoto.title,
                            color = Color(0xFFF6F6FA),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${currentPhoto.bucketName} · ${currentPhoto.mimeType}",
                            color = Color(0xFF8E8E9A),
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                        Text(
                            text = GalleryUtils.formatDateTime(currentPhoto.epochMillis),
                            color = Color(0xFF8E8E9A),
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                exifRows.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { (label, value) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1D1D28))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = label.uppercase(),
                                    color = Color(0xFF8E8E9A),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                                Text(
                                    text = value,
                                    color = Color(0xFFF6F6FA),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Developer Diagnostics: Full Path, Content URI & Raw EXIF tags
                if (uiState.developerSettings.showRawExifAndPath) {
                    Text(
                        text = "DEVELOPER FILE PATH & URI",
                        color = Color(0xFF8B6BFF),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.9.sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1D1D28))
                            .border(0.75.dp, Color(0x408B6BFF), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Path: ${currentPhoto.relativePath}",
                            color = Color(0xFFF6F6FA),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "URI: ${currentPhoto.uri}",
                            color = Color(0xFFB4B4C4),
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        currentPhoto.mediaStoreId?.let { msId ->
                            Text(
                                text = "MediaStore ID: $msId · Bucket: ${currentPhoto.bucketId}",
                                color = Color(0xFF8E8E9A),
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (exif.rawTags.isNotEmpty()) {
                        Text(
                            text = "RAW EXIF TAGS",
                            color = Color(0xFF8B6BFF),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.9.sp,
                            modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
                        )
                        exif.rawTags.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { (tag, valStr) ->
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1D1D28))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            color = Color(0xFF8E8E9A),
                                            fontSize = 9.5.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = valStr,
                                            color = Color(0xFFF6F6FA),
                                            fontSize = 11.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                if (related.isNotEmpty()) {
                    Text(
                        text = "MORE IN ${currentPhoto.bucketName.uppercase()}",
                        color = Color(0xFF8E8E9A),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.9.sp,
                        modifier = Modifier.padding(top = 14.dp, bottom = 9.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(related, key = { it.id }) { rel ->
                            SmartImage(
                                uri = rel.thumb,
                                contentDescription = rel.title,
                                cornerRadius = 12.dp,
                                modifier = Modifier
                                    .size(82.dp)
                                    .clickable {
                                        infoOpen = false
                                        val idx = photos.indexOfFirst { it.id == rel.id }
                                        if (idx >= 0) {
                                            scope.launch { pagerState.animateScrollToPage(idx) }
                                        } else {
                                            onOpenPhoto(rel.id)
                                        }
                                    }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1D1D28))
                            .clickable { viewModel.toggleFavorite(currentPhoto.id) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) Color(0xFFFF5C8A) else Color(0xFFF6F6FA),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = if (isFavorite) "Favorited" else "Favorite",
                            color = Color(0xFFF6F6FA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1D1D28))
                            .clickable {
                                GalleryUtils.sharePhoto(context, currentPhoto, uiState.appNameLabel)
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            tint = Color(0xFFF6F6FA),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = "Share",
                            color = Color(0xFFF6F6FA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Add to album sheet
    if (albumOpen) {
        ModalBottomSheet(
            onDismissRequest = { albumOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF14141D)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Add to album",
                    color = Color(0xFFF6F6FA),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (uiState.userAlbums.isEmpty()) {
                    Text(
                        text = "No custom albums yet. Create one from the Albums tab, then come back to add this photo.",
                        color = Color(0xFF8E8E9A),
                        fontSize = 12.5.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF6F6FA))
                            .clickable {
                                albumOpen = false
                                onOpenAlbumsTab()
                            }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Go to albums",
                            color = Color(0xFF0B0B12),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    uiState.userAlbums.forEach { album ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.addPhotosToAlbum(album.id, listOf(currentPhoto.id))
                                    albumOpen = false
                                    toastMessage = "Added to ${album.title}"
                                }
                                .padding(vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = album.emoji, fontSize = 22.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = album.title,
                                    color = Color(0xFFF6F6FA),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${album.photoIds.size} photos",
                                    color = Color(0xFF8E8E9A),
                                    fontSize = 11.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.CreateNewFolder,
                                contentDescription = null,
                                tint = Color(0xFF8E8E9A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Set as Wallpaper sheet
    if (wallpaperOpen) {
        ModalBottomSheet(
            onDismissRequest = { wallpaperOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF14141D)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Set as wallpaper",
                    color = Color(0xFFF6F6FA),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Choose where to apply “${currentPhoto.title}”.",
                    color = Color(0xFF8E8E9A),
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                WallpaperTarget.entries.forEach { target ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1D1D28))
                            .clickable {
                                wallpaperOpen = false
                                toastMessage = "Setting ${target.label} wallpaper…"
                                scope.launch {
                                    val ok = GalleryUtils.setWallpaper(context, currentPhoto, target)
                                    toastMessage = if (ok) "${target.label} wallpaper updated" else "Could not set wallpaper"
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Wallpaper,
                            contentDescription = null,
                            tint = Color(0xFF8B6BFF),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = target.label,
                            color = Color(0xFFF6F6FA),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation sheet
    if (deleteOpen) {
        ModalBottomSheet(
            onDismissRequest = { deleteOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF14141D)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Delete from device?",
                    color = Color(0xFFF6F6FA),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "“${currentPhoto.title}” will be permanently deleted from your device storage.",
                    color = Color(0xFF8E8E9A),
                    fontSize = 12.5.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1D1D28))
                            .clickable { deleteOpen = false }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", color = Color(0xFFF6F6FA), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFF5D5D))
                            .clickable {
                                val target = currentPhoto
                                deleteOpen = false
                                viewModel.requestDeletePhotos(listOf(target)) { outcome ->
                                    when (outcome) {
                                        is DeleteOutcome.Deleted -> {
                                            toastMessage = "Deleted from device"
                                            if (photos.size <= 1) onBack()
                                        }
                                        is DeleteOutcome.RequiresConfirmation -> {
                                            deleteSenderLauncher.launch(
                                                IntentSenderRequest.Builder(outcome.intentSender).build()
                                            )
                                        }
                                        is DeleteOutcome.Failed -> {
                                            toastMessage = "Could not delete photo"
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Delete", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
