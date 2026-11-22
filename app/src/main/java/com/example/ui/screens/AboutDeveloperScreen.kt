package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GalleryUiState
import com.example.ui.components.AvatarCircle
import com.example.ui.components.XPhotosIconButton
import com.example.ui.theme.XPhotosGradients
import com.example.ui.theme.xPalette
import kotlinx.coroutines.delay

@Composable
fun AboutDeveloperScreen(
    uiState: GalleryUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.xPalette
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    var topSecretExpanded by remember { mutableStateOf(false) }
    var copiedMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(copiedMessage) {
        if (copiedMessage != null) {
            delay(1800)
            copiedMessage = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                XPhotosIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                    modifier = Modifier.testTag("about_dev_back")
                )
                Column {
                    Text(
                        text = "CREATOR & CREDITS",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = palette.textMuted
                    )
                    Text(
                        text = "About Developer",
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.text
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Developer Bio Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(palette.surface)
                            .border(0.75.dp, palette.border, RoundedCornerShape(22.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            palette.primary.copy(alpha = 0.22f),
                                            palette.accent.copy(alpha = 0.08f)
                                        )
                                    )
                                )
                        )
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                AvatarCircle(
                                    name = "Raunak Yadav",
                                    size = 64.dp,
                                    gradientColors = listOf(palette.primary, palette.accent)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Raunak Yadav",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 21.sp),
                                        color = palette.text
                                    )
                                    Text(
                                        text = "Lead Android Developer & Designer",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = palette.primary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "Built ${uiState.appNameLabel} to be a private, lightning-fast, 100% offline Android photo gallery — zero cloud tracking, zero subscriptions, complete local control.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textSecondary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Contact & Social Details (Offline Copyable)
                item {
                    Column {
                        Text(
                            text = "CONTACT & HANDLES",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = palette.textMuted,
                            modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(palette.surface)
                                .border(0.75.dp, palette.border, RoundedCornerShape(22.dp))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            ContactRow(
                                icon = Icons.Rounded.CameraAlt,
                                label = "Instagram",
                                value = "@raunak_yadav",
                                onCopy = {
                                    clipboard.setText(AnnotatedString("@raunak_yadav"))
                                    copiedMessage = "Instagram handle copied"
                                }
                            )
                            HorizontalDivider(thickness = 0.75.dp, color = palette.border)
                            ContactRow(
                                icon = Icons.Rounded.Email,
                                label = "Email",
                                value = "raunakraj93344@gmail.com",
                                onCopy = {
                                    clipboard.setText(AnnotatedString("raunakraj93344@gmail.com"))
                                    copiedMessage = "Email copied"
                                }
                            )
                            HorizontalDivider(thickness = 0.75.dp, color = palette.border)
                            ContactRow(
                                icon = Icons.Rounded.Code,
                                label = "GitHub",
                                value = "raunakraj93344-netizen",
                                onCopy = {
                                    clipboard.setText(AnnotatedString("raunakraj93344-netizen"))
                                    copiedMessage = "GitHub username copied"
                                }
                            )
                            HorizontalDivider(thickness = 0.75.dp, color = palette.border)
                            ContactRow(
                                icon = Icons.Rounded.AlternateEmail,
                                label = "Build Signature",
                                value = "Offline Edition · Kotlin & Jetpack Compose",
                                onCopy = {
                                    clipboard.setText(AnnotatedString("Offline Edition · Kotlin & Jetpack Compose"))
                                    copiedMessage = "Build signature copied"
                                }
                            )
                        }
                    }
                }

                // Top Secret Slide-Reveal Menu Item
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Brush.linearGradient(XPhotosGradients.secretBanner))
                            .border(0.75.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(22.dp))
                            .clickable { topSecretExpanded = !topSecretExpanded }
                            .padding(16.dp)
                            .testTag("top_secret_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x28FFD479)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Key,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD479),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Top Secret",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (topSecretExpanded) "Tap to hide classified instructions" else "Tap to slide open secret instructions",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.5.sp
                                )
                            }
                            Icon(
                                imageVector = if (topSecretExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = "Toggle Top Secret",
                                tint = Color(0xFFFFD479),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = topSecretExpanded,
                            enter = slideInVertically { -it / 2 } + expandVertically() + fadeIn(),
                            exit = slideOutVertically { -it / 2 } + shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.35f))
                                    .border(0.75.dp, Color(0x55FFD479), RoundedCornerShape(16.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD479),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "UNLOCK INSTRUCTIONS",
                                        color = Color(0xFFFFD479),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = "1. Developer Options: Go back to Settings and tap App Version 7 times quickly, then enter the secret developer code to unlock Developer Options.",
                                    color = Color.White.copy(alpha = 0.92f),
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp
                                )
                                Text(
                                    text = "2. Private Hidden Vault: Press and hold App Version in Settings for 3–4 seconds to set up or enter your 4-digit PIN and open hidden photos.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = copiedMessage != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(palette.text)
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Text(
                    text = copiedMessage.orEmpty(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.bg
                )
            }
        }
    }
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    val palette = MaterialTheme.xPalette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCopy)
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
                tint = palette.textSecondary,
                modifier = Modifier.size(17.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = palette.textMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.text
            )
        }
        Icon(
            imageVector = Icons.Rounded.ContentCopy,
            contentDescription = "Copy $label",
            tint = palette.textMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}
