package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.XPhotosGradients
import com.example.ui.theme.xPalette
import com.example.util.GalleryUtils
import java.io.File

@Composable
fun SmartImage(
    uri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    cornerRadius: Dp = 0.dp,
    transparentBackground: Boolean = false
) {
    val palette = MaterialTheme.xPalette
    val context = LocalContext.current
    var failed by remember(uri) { mutableStateOf(false) }

    val model = remember(uri) {
        when {
            uri.isBlank() -> null
            uri.startsWith("file://") -> File(android.net.Uri.parse(uri).path ?: "")
            uri.startsWith("content://") -> android.net.Uri.parse(uri)
            else -> uri
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (transparentBackground) Color.Transparent else palette.surfaceAlt),
        contentAlignment = Alignment.Center
    ) {
        if (failed || model == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(palette.primary, palette.accent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.BrokenImage,
                    contentDescription = contentDescription,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(26.dp)
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(model)
                    .crossfade(220)
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                onError = { failed = true },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun BrandHeader(
    appName: String = "XPhotos",
    modifier: Modifier = Modifier,
    showTagline: Boolean = false
) {
    val palette = MaterialTheme.xPalette
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    Brush.linearGradient(
                        listOf(palette.primary, palette.accent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Camera,
                contentDescription = "$appName logo",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = appName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = palette.text
            )
            if (showTagline) {
                Text(
                    text = "Offline device gallery",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textMuted
                )
            }
        }
    }
}

enum class IconButtonVariant {
    surface,
    ghost,
    solid
}

@Composable
fun XPhotosIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    variant: IconButtonVariant = IconButtonVariant.surface,
    tint: Color? = null
) {
    val palette = MaterialTheme.xPalette
    val bgColor = when (variant) {
        IconButtonVariant.surface -> palette.surfaceAlt
        IconButtonVariant.ghost -> Color.White.copy(alpha = 0.14f)
        IconButtonVariant.solid -> palette.text
    }
    val iconColor = tint ?: when (variant) {
        IconButtonVariant.surface -> palette.text
        IconButtonVariant.ghost -> Color.White
        IconButtonVariant.solid -> palette.bg
    }
    val borderMod = if (variant == IconButtonVariant.surface) {
        Modifier.border(0.75.dp, palette.border, CircleShape)
    } else Modifier

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .then(borderMod)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun AvatarCircle(
    name: String,
    size: Dp = 40.dp,
    gradientColors: List<Color> = XPhotosGradients.violet,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.xPalette
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradientColors))
            .border(0.75.dp, palette.border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = GalleryUtils.initials(name),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (size.value * 0.36f).sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun XPhotosChip(
    label: String,
    active: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val palette = MaterialTheme.xPalette
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (active) palette.text else palette.surfaceAlt)
            .border(0.75.dp, if (active) palette.text else palette.border, CircleShape)
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 8.dp else 10.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = if (compact) 12.sp else 13.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = if (active) palette.bg else palette.textSecondary,
            maxLines = 1
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val palette = MaterialTheme.xPalette
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = palette.text
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun StatTile(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.xPalette
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surface)
            .border(0.75.dp, palette.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            color = palette.text
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted
        )
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val palette = MaterialTheme.xPalette
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(palette.surfaceAlt)
                .border(0.75.dp, palette.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.textMuted,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = palette.text,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(palette.text)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 22.dp, vertical = 12.dp)
                    .testTag("empty_state_action")
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.bg
                )
            }
        }
    }
}

@Composable
fun <T> SegmentedControl(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.xPalette
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(palette.surfaceAlt)
            .border(0.75.dp, palette.border, CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEach { (key, label) ->
            val active = key == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (active) palette.surface else Color.Transparent)
                    .then(
                        if (active) Modifier.border(0.75.dp, palette.border, CircleShape)
                        else Modifier
                    )
                    .clickable { onSelect(key) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = if (active) palette.text else palette.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SettingRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    valueText: String? = null,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    danger: Boolean = false,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.xPalette
    val titleColor = if (danger) palette.danger else palette.text
    val iconColor = if (danger) palette.danger else palette.textSecondary

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.surfaceAlt),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(17.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = titleColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = palette.textMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (checked != null && onCheckedChange != null) {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = palette.primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = palette.borderStrong
                    )
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (valueText != null) {
                        Text(
                            text = valueText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textMuted
                        )
                    }
                    if (onClick != null) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = palette.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(thickness = 0.75.dp, color = palette.border)
        }
    }
}
