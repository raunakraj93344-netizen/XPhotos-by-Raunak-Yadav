package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AppRegistration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AccentPreset
import com.example.model.AppNameOption
import com.example.ui.GalleryUiState
import com.example.ui.GalleryViewModel
import com.example.ui.components.SegmentedControl
import com.example.ui.components.SettingRowItem
import com.example.ui.components.XPhotosIconButton
import com.example.ui.theme.xPalette
import com.example.util.GalleryUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeveloperOptionsScreen(
    uiState: GalleryUiState,
    viewModel: GalleryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.xPalette
    val context = LocalContext.current
    val dev = uiState.developerSettings
    val diagnostics = remember { GalleryUtils.getDeviceDiagnostics(context) }

    var resetConfirmOpen by remember { mutableStateOf(false) }
    var toastText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toastText) {
        if (toastText != null) {
            delay(2000)
            toastText = null
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
                    modifier = Modifier.testTag("dev_options_back")
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RAUNAKxYADAV · UNLOCKED",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = palette.primary
                    )
                    Text(
                        text = "Developer Options",
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.text
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(palette.surfaceAlt)
                        .border(0.75.dp, palette.border, CircleShape)
                        .clickable {
                            viewModel.lockDeveloperOptions()
                            onBack()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("lock_developer_options_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = palette.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Lock",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.text
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. App Name Switcher (Screen + Home Screen Icon)
                item {
                    Column {
                        Text(
                            text = "APP NAME & LAUNCHER ICON",
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
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AppRegistration,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Select active app name",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = palette.text
                                )
                            }
                            Text(
                                text = "Updates all on-screen titles instantly and switches your home-screen icon label to one of the 5 allowed presets.",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textMuted,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppNameOption.entries.forEach { option ->
                                    val selected = dev.appName == option
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (selected) palette.primary else palette.surfaceAlt)
                                            .border(
                                                0.75.dp,
                                                if (selected) palette.primary else palette.border,
                                                RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                viewModel.switchAppName(option)
                                                toastText = "App name set to ${option.displayName}"
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                            .testTag("app_name_option_${option.name}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = option.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (selected) Color.White else palette.text
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Performance & Diagnostics
                item {
                    Column {
                        Text(
                            text = "PERFORMANCE & DIAGNOSTICS",
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
                            SettingRowItem(
                                icon = Icons.Rounded.Speed,
                                title = "Live FPS & RAM Monitor HUD",
                                subtitle = "Show real-time frame rate and heap usage overlay",
                                checked = dev.showFpsRamHud,
                                onCheckedChange = { viewModel.setShowFpsRamHud(it) },
                                modifier = Modifier.testTag("toggle_fps_hud")
                            )
                            SettingRowItem(
                                icon = Icons.Rounded.Code,
                                title = "Photo file path & raw EXIF",
                                subtitle = "Expose disk paths, URIs & raw camera tags in details",
                                checked = dev.showRawExifAndPath,
                                onCheckedChange = { viewModel.setShowRawExifAndPath(it) },
                                showDivider = false,
                                modifier = Modifier.testTag("toggle_raw_exif")
                            )
                        }
                    }
                }

                // 3. Visual & Slideshow Customization
                item {
                    Column {
                        Text(
                            text = "THEME & ANIMATION TWEAKS",
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
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "App Accent Color (${dev.accentPreset.label})",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.text
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AccentPreset.entries.forEach { preset ->
                                    val active = dev.accentPreset == preset
                                    val swatch = Color(
                                        if (palette.isDark) preset.darkColorHex else preset.lightColorHex
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(swatch)
                                            .border(
                                                width = if (active) 3.dp else 1.dp,
                                                color = if (active) palette.text else Color.White.copy(alpha = 0.25f),
                                                shape = CircleShape
                                            )
                                            .clickable { viewModel.setAccentPreset(preset) }
                                            .testTag("accent_preset_${preset.name}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (active) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = preset.label,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(thickness = 0.75.dp, color = palette.border)

                            SettingRowItem(
                                icon = Icons.Rounded.Contrast,
                                title = "Pure AMOLED Black Mode",
                                subtitle = "True #000000 surfaces when Dark theme is active",
                                checked = dev.pureAmoledDark,
                                onCheckedChange = { viewModel.setPureAmoledDark(it) },
                                modifier = Modifier.testTag("toggle_amoled_dark")
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Animation Speed Scale",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.text
                                )
                                SegmentedControl(
                                    options = listOf(
                                        0.4f to "Fast 0.4×",
                                        1.0f to "Normal 1×",
                                        1.6f to "Smooth 1.6×"
                                    ),
                                    selected = dev.animationSpeedScale,
                                    onSelect = { viewModel.setAnimationSpeedScale(it) }
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Slideshow Frame Interval",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.text
                                )
                                SegmentedControl(
                                    options = listOf(
                                        2500 to "2.5s",
                                        4600 to "4.6s",
                                        7000 to "7s",
                                        10000 to "10s"
                                    ),
                                    selected = dev.slideshowIntervalMs,
                                    onSelect = { viewModel.setSlideshowIntervalMs(it) }
                                )
                            }

                            SettingRowItem(
                                icon = Icons.Rounded.Shuffle,
                                title = "Shuffle Slideshow Order",
                                subtitle = "Randomize photo sequence during playback",
                                checked = dev.slideshowShuffle,
                                onCheckedChange = { viewModel.setSlideshowShuffle(it) },
                                showDivider = false
                            )
                        }
                    }
                }

                // 4. Device & Android System Info
                item {
                    Column {
                        Text(
                            text = "DEVICE & ANDROID SYSTEM INFO",
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
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeveloperBoard,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${diagnostics.manufacturer} ${diagnostics.model}",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = palette.text
                                )
                            }
                            val infoRows = listOf(
                                "Android OS" to "${diagnostics.androidVersion} (API ${diagnostics.sdkInt})",
                                "Package ID" to diagnostics.packageName,
                                "Version" to "${diagnostics.versionName} (${uiState.appNameLabel})",
                                "Display" to diagnostics.screenMetrics,
                                "CPU ABIs" to diagnostics.supportedAbis,
                                "Device Free Space" to "${GalleryUtils.formatBytes(diagnostics.availableStorageBytes)} / ${GalleryUtils.formatBytes(diagnostics.totalStorageBytes)}",
                                "Media Access" to if (uiState.hasMediaPermission) "Granted (Offline MediaStore)" else "Not granted",
                                "Indexed Photos" to "${uiState.stats.totalPhotos} visible · ${uiState.stats.hiddenCount} hidden"
                            )
                            infoRows.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pair.forEach { (label, value) ->
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(palette.surfaceAlt)
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = label.uppercase(),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = palette.textMuted
                                            )
                                            Text(
                                                text = value,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = palette.text,
                                                modifier = Modifier.padding(top = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Vault & Reset Controls
                item {
                    Column(modifier = Modifier.padding(bottom = 28.dp)) {
                        Text(
                            text = "DEVELOPER ACTIONS",
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
                            SettingRowItem(
                                icon = Icons.Rounded.LockReset,
                                title = "Clear Private Vault PIN",
                                subtitle = if (uiState.hasVaultPin) "Remove current 4-digit PIN so you can set a new one" else "No Vault PIN set yet",
                                onClick = {
                                    viewModel.clearVaultPin()
                                    toastText = "Vault PIN cleared"
                                }
                            )
                            SettingRowItem(
                                icon = Icons.Rounded.DeleteSweep,
                                title = "Reset all local data & settings",
                                subtitle = "Restores defaults and locks Developer Options",
                                danger = true,
                                showDivider = false,
                                onClick = { resetConfirmOpen = true }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = toastText != null,
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
                    text = toastText.orEmpty(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.bg
                )
            }
        }
    }

    if (resetConfirmOpen) {
        ModalBottomSheet(
            onDismissRequest = { resetConfirmOpen = false },
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
                    text = "Reset all app data?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.text
                )
                Text(
                    text = "Clears custom albums, favorites, Vault PIN, Developer Options settings, and restores the app name to XPhotos.",
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
                            .clickable { resetConfirmOpen = false }
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
                                viewModel.resetData()
                                resetConfirmOpen = false
                                onBack()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Reset Everything", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }
            }
        }
    }
}
