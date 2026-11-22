package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GalleryUiState
import com.example.ui.GalleryViewModel
import com.example.ui.components.IconButtonVariant
import com.example.ui.components.SmartImage
import com.example.ui.components.XPhotosIconButton
import com.example.util.GalleryUtils
import kotlinx.coroutines.delay

@Composable
fun SlideshowScreen(
    startPhotoId: String?,
    albumId: String?,
    uiState: GalleryUiState,
    viewModel: GalleryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val slideDurationMs = uiState.developerSettings.slideshowIntervalMs.coerceIn(1500, 12000)
    val shuffleEnabled = uiState.developerSettings.slideshowShuffle

    val list = remember(albumId, uiState.photos, shuffleEnabled) {
        val base = if (!albumId.isNullOrBlank()) {
            viewModel.resolveAlbum(albumId)?.photos ?: uiState.photos
        } else {
            uiState.photos
        }
        if (shuffleEnabled) base.shuffled() else base
    }

    if (list.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nothing to play yet", color = Color.White)
                Text(
                    text = "Go back",
                    color = Color(0xFF8B6BFF),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .clickable(onClick = onBack)
                )
            }
        }
        return
    }

    val initialIdx = remember(list, startPhotoId) {
        list.indexOfFirst { it.id == startPhotoId }.coerceAtLeast(0)
    }

    var index by remember { mutableIntStateOf(initialIdx) }
    var playing by remember { mutableStateOf(uiState.settings.autoplay) }
    var chrome by remember { mutableStateOf(true) }

    val safeIndex = index.coerceIn(0, list.lastIndex)
    val photo = list[safeIndex]
    val isFavorite = uiState.isFavorite(photo.id)

    val kenBurnsScale = remember { Animatable(1f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(safeIndex, playing, slideDurationMs) {
        kenBurnsScale.snapTo(1f)
        if (playing) {
            kenBurnsScale.animateTo(
                targetValue = 1.12f,
                animationSpec = tween(durationMillis = slideDurationMs + 400, easing = LinearEasing)
            )
        }
    }

    LaunchedEffect(safeIndex, playing, list.size, slideDurationMs) {
        progress.snapTo(0f)
        if (playing && list.size > 1) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = slideDurationMs, easing = LinearEasing)
            )
            index = (safeIndex + 1) % list.size
        } else if (playing) {
            delay(slideDurationMs.toLong())
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { chrome = !chrome }
    ) {
        AnimatedContent(
            targetState = photo,
            transitionSpec = { fadeIn(tween(550)) togetherWith fadeOut(tween(450)) },
            label = "slideshow_frame",
            modifier = Modifier.fillMaxSize()
        ) { frame ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = kenBurnsScale.value
                        scaleY = kenBurnsScale.value
                    }
            ) {
                SmartImage(
                    uri = frame.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(32.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                )
                SmartImage(
                    uri = frame.uri,
                    contentDescription = frame.title,
                    contentScale = ContentScale.Fit,
                    transparentBackground = true,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        AnimatedVisibility(
            visible = chrome,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top controls + progress
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        XPhotosIconButton(
                            icon = Icons.Rounded.Close,
                            contentDescription = "Close slideshow",
                            variant = IconButtonVariant.ghost,
                            onClick = onBack,
                            modifier = Modifier.testTag("slideshow_close")
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${safeIndex + 1} / ${list.size}",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            XPhotosIconButton(
                                icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favorite",
                                variant = IconButtonVariant.ghost,
                                tint = if (isFavorite) Color(0xFFFF5C8A) else Color.White,
                                onClick = { viewModel.toggleFavorite(photo.id) }
                            )
                            XPhotosIconButton(
                                icon = Icons.Rounded.Share,
                                contentDescription = "Share",
                                variant = IconButtonVariant.ghost,
                                onClick = {
                                    GalleryUtils.sharePhoto(context, photo, uiState.appNameLabel)
                                }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress.value)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }

                // Bottom caption & playback controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f),
                                    Color.Black.copy(alpha = 0.95f)
                                )
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = photo.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${photo.bucketName} · ${GalleryUtils.formatBytes(GalleryUtils.estimateSize(photo))}",
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = GalleryUtils.formatDate(photo.epochMillis),
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Row(
                        modifier = Modifier.padding(top = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .clickable { index = (safeIndex - 1 + list.size) % list.size },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous photo", tint = Color.White, modifier = Modifier.size(24.dp))
                        }

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { playing = !playing }
                                .testTag("slideshow_play_pause"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (playing) "Pause slideshow" else "Play slideshow",
                                tint = Color(0xFF0B0B12),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .clickable { index = (safeIndex + 1) % list.size },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.ChevronRight, contentDescription = "Next photo", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }

                    Text(
                        text = "${if (playing) "Playing (${slideDurationMs / 1000f}s)" else "Paused · tap play to continue"} · tap anywhere to hide controls",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}
