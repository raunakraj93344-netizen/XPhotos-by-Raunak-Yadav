package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GradientKey
import com.example.model.Photo
import com.example.ui.theme.XPhotosGradients
import com.example.ui.theme.xPalette

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoCard(
    photo: Photo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    aspectRatio: Float? = null,
    height: Dp? = null,
    selected: Boolean = false,
    selectionMode: Boolean = false,
    favorite: Boolean = false,
    showLabel: Boolean = true,
    cornerRadius: Dp = 16.dp
) {
    val palette = MaterialTheme.xPalette
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(cornerRadius)

    val sizeModifier = when {
        height != null -> Modifier.fillMaxWidth().height(height)
        aspectRatio != null -> Modifier.fillMaxWidth().aspectRatio(aspectRatio)
        else -> {
            val ratio = (photo.width.toFloat() / photo.height.toFloat()).coerceIn(0.65f, 1.38f)
            Modifier.fillMaxWidth().aspectRatio(ratio)
        }
    }

    Box(
        modifier = modifier
            .then(sizeModifier)
            .clip(shape)
            .background(Color(0xFF111111))
            .border(
                width = if (selected) 3.dp else 0.75.dp,
                color = if (selected) palette.primary else palette.border,
                shape = shape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    if (onLongClick != null) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                }
            )
            .testTag("photo_card_${photo.id}")
    ) {
        SmartImage(
            uri = photo.thumb,
            contentDescription = photo.title,
            cornerRadius = cornerRadius,
            modifier = Modifier.fillMaxSize()
        )

        if (showLabel) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 9.dp)
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = photo.title,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = photo.bucketName,
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (favorite && !selectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "Favorited",
                    tint = Color(0xFFFF6F91),
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        if (selectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (selected) palette.primary else Color.Black.copy(alpha = 0.35f))
                    .border(
                        width = 1.5.dp,
                        color = if (selected) palette.primary else Color.White.copy(alpha = 0.85f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumCard(
    title: String,
    count: Int,
    uri: String?,
    emoji: String,
    gradient: GradientKey = GradientKey.violet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 132.dp
) {
    val palette = MaterialTheme.xPalette
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(palette.surface)
            .border(0.75.dp, palette.border, shape)
            .clickable(onClick = onClick)
    ) {
        if (!uri.isNullOrBlank()) {
            SmartImage(
                uri = uri,
                contentDescription = title,
                cornerRadius = 16.dp,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(XPhotosGradients.of(gradient)))
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color.Black.copy(alpha = 0.42f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 14.sp)
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$count ${if (count == 1) "photo" else "photos"}",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MemoryCard(
    uri: String,
    count: Int,
    onClick: () -> Unit,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(172.dp)
            .clip(shape)
            .clickable(onClick = onClick)
            .testTag("memory_card")
    ) {
        SmartImage(
            uri = uri,
            contentDescription = "Recent memories",
            cornerRadius = 22.dp,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.86f))
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFFFD479),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "LOCAL MEMORIES",
                color = Color(0xFFFFD479),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp, end = 104.dp)
        ) {
            Text(
                text = "Recent highlights",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "$count photos stored on your device",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.95f))
                .clickable(onClick = onReplay)
                .padding(horizontal = 16.dp, vertical = 9.dp)
                .testTag("memory_replay_button")
        ) {
            Text(
                text = "Slideshow",
                color = Color(0xFF0B0B12),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
