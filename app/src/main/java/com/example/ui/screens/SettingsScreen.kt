package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GridDensity
import com.example.model.ThemeMode
import com.example.ui.GalleryUiState
import com.example.ui.GalleryViewModel
import com.example.ui.components.SegmentedControl
import com.example.ui.components.SettingRowItem
import com.example.ui.theme.xPalette
import com.example.util.GalleryUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val storageColors = listOf(
    Color(0xFF8B6BFF),
    Color(0xFFFF5C8A),
    Color(0xFFFFC24B),
    Color(0xFF3DDC97),
    Color(0xFF2E7BFF)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: GalleryUiState,
    viewModel: GalleryViewModel,
    onOpenAboutDeveloper: () -> Unit,
    onOpenDeveloperOptions: () -> Unit,
    onOpenHiddenVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.xPalette
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var resetOpen by remember { mutableStateOf(false) }
    var codeDialogOpen by remember { mutableStateOf(false) }
    var codeInput by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }

    var pinSheetOpen by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinConfirmInput by remember { mutableStateOf("") }
    var pinSetupStep by remember { mutableIntStateOf(0) } // 0 = create, 1 = confirm
    var pinError by remember { mutableStateOf<String?>(null) }

    var versionTapCount by remember { mutableIntStateOf(0) }
    var lastTapMillis by remember { mutableLongStateOf(0L) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isHoldingVersion by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(2200)
            statusMessage = null
        }
    }

    val topFolders = remember(uiState.stats.folderStats) {
        uiState.stats.folderStats.take(5)
    }
    val totalCount = uiState.stats.totalPhotos.coerceAtLeast(1)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "OFFLINE GALLERY",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                            color = palette.textMuted
                        )
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.displaySmall,
                            color = palette.text
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(CircleShape)
                            .background(palette.primarySoft)
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.OfflinePin,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Local only",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.primary
                        )
                    }
                }
            }

            // Local Storage Insights
            item {
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "DEVICE STORAGE",
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(palette.primarySoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Storage,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${GalleryUtils.formatBytes(uiState.stats.storageBytes)} indexed",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = palette.text
                                )
                                Text(
                                    text = "${uiState.stats.totalPhotos} photos · ${uiState.stats.hiddenCount} in vault · ${uiState.userAlbums.size} albums",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                    color = palette.textMuted,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        if (topFolders.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(palette.surfaceAlt),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                topFolders.forEachIndexed { index, entry ->
                                    val weight = (entry.count.toFloat() / totalCount.toFloat()).coerceAtLeast(0.05f)
                                    Box(
                                        modifier = Modifier
                                            .weight(weight)
                                            .height(10.dp)
                                            .background(storageColors[index % storageColors.size])
                                    )
                                }
                                val remaining = (1f - topFolders.sumOf { it.count }.toFloat() / totalCount.toFloat()).coerceAtLeast(0.02f)
                                Spacer(Modifier.weight(remaining))
                            }

                            Column(
                                modifier = Modifier.padding(top = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                topFolders.chunked(2).forEachIndexed { rowIdx, pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        pair.forEachIndexed { colIdx, entry ->
                                            val idx = rowIdx * 2 + colIdx
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(storageColors[idx % storageColors.size])
                                                )
                                                Text(
                                                    text = entry.folderName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = palette.textSecondary,
                                                    maxLines = 1,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${entry.count}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = palette.textMuted
                                                )
                                            }
                                        }
                                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Appearance
            item {
                Column(
                    modifier = Modifier
                        .padding(top = 26.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "APPEARANCE",
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
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = null,
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Theme",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.text
                                )
                            }
                            SegmentedControl(
                                options = ThemeMode.entries.map { it to it.label },
                                selected = uiState.settings.themeMode,
                                onSelect = { viewModel.setThemeMode(it) },
                                modifier = Modifier.testTag("theme_segmented_control")
                            )
                        }

                        HorizontalDivider(thickness = 0.75.dp, color = palette.border)

                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PhotoLibrary,
                                    contentDescription = null,
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Grid density",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.text
                                )
                            }
                            SegmentedControl(
                                options = GridDensity.entries.map { it to it.label },
                                selected = uiState.settings.density,
                                onSelect = { viewModel.setDensity(it) },
                                modifier = Modifier.testTag("density_segmented_control")
                            )
                        }

                        HorizontalDivider(thickness = 0.75.dp, color = palette.border)

                        SettingRowItem(
                            icon = Icons.Rounded.AutoAwesome,
                            title = "Autoplay slideshows",
                            subtitle = "Start playing slideshows automatically",
                            checked = uiState.settings.autoplay,
                            onCheckedChange = { viewModel.setAutoplay(it) }
                        )

                        SettingRowItem(
                            icon = Icons.Rounded.Info,
                            title = "Show photo metadata",
                            subtitle = "Display folder and date details in viewer",
                            checked = uiState.settings.showMeta,
                            onCheckedChange = { viewModel.setShowMeta(it) },
                            showDivider = false
                        )
                    }
                }
            }

            // About & Developer
            item {
                Column(
                    modifier = Modifier
                        .padding(top = 26.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "ABOUT & SYSTEM",
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
                            icon = Icons.Rounded.Person,
                            title = "About Developer",
                            subtitle = "Creator info, contact & Top Secret",
                            onClick = onOpenAboutDeveloper,
                            modifier = Modifier.testTag("about_developer_row")
                        )

                        if (uiState.developerSettings.unlocked) {
                            SettingRowItem(
                                icon = Icons.Rounded.DeveloperMode,
                                title = "Developer Options",
                                subtitle = "App name, HUD, theme tweaks & diagnostics",
                                valueText = "Unlocked",
                                onClick = onOpenDeveloperOptions,
                                modifier = Modifier.testTag("developer_options_row")
                            )
                        }

                        // App Version Row: 7 quick taps -> Developer code; 3s long press -> Hidden Vault PIN
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(uiState.developerSettings.unlocked, uiState.hasVaultPin) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown()
                                        val pressStart = System.currentTimeMillis()
                                        var longTriggered = false
                                        isHoldingVersion = true
                                        holdProgress = 0f

                                        val holdJob = scope.launch {
                                            val targetMs = 3000L
                                            while (true) {
                                                val elapsed = System.currentTimeMillis() - pressStart
                                                holdProgress = (elapsed.toFloat() / targetMs).coerceIn(0f, 1f)
                                                if (elapsed >= targetMs) {
                                                    longTriggered = true
                                                    isHoldingVersion = false
                                                    holdProgress = 0f
                                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    pinInput = ""
                                                    pinConfirmInput = ""
                                                    pinError = null
                                                    pinSetupStep = 0
                                                    pinSheetOpen = true
                                                    break
                                                }
                                                delay(40)
                                            }
                                        }

                                        var cancelledByMove = false
                                        val touchSlop = viewConfiguration.touchSlop
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull { it.id == down.id }
                                            if (change == null || !change.pressed) {
                                                break
                                            }
                                            if (change.isConsumed || (change.position - down.position).getDistance() > touchSlop) {
                                                cancelledByMove = true
                                                break
                                            }
                                        }
                                        holdJob.cancel()
                                        isHoldingVersion = false
                                        holdProgress = 0f

                                        val duration = System.currentTimeMillis() - pressStart
                                        if (!longTriggered && !cancelledByMove && duration < 450L) {
                                            val now = System.currentTimeMillis()
                                            versionTapCount = if (now - lastTapMillis > 2500L) 1 else versionTapCount + 1
                                            lastTapMillis = now

                                            if (uiState.developerSettings.unlocked) {
                                                if (versionTapCount >= 3) {
                                                    statusMessage = "Developer Options already unlocked"
                                                    versionTapCount = 0
                                                }
                                            } else {
                                                when {
                                                    versionTapCount in 3..6 -> {
                                                        val left = 7 - versionTapCount
                                                        statusMessage = "$left more ${if (left == 1) "tap" else "taps"} to enter Developer code"
                                                    }
                                                    versionTapCount >= 7 -> {
                                                        versionTapCount = 0
                                                        codeInput = ""
                                                        codeError = null
                                                        codeDialogOpen = true
                                                    }
                                                }
                                            }
                                        }
                                        down.consume()
                                    }
                                }
                                .padding(vertical = 13.dp)
                                .testTag("app_version_row")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                        imageVector = Icons.Rounded.Code,
                                        contentDescription = null,
                                        tint = palette.textSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "App Version",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = palette.text
                                    )
                                    Text(
                                        text = "${uiState.appNameLabel} v1.0.0 · Offline Build",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                        color = palette.textMuted,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = palette.textMuted.copy(alpha = 0.55f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            AnimatedVisibility(visible = isHoldingVersion && holdProgress > 0.05f) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    LinearProgressIndicator(
                                        progress = { holdProgress },
                                        color = palette.primary,
                                        trackColor = palette.surfaceAlt,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(CircleShape)
                                    )
                                    Text(
                                        text = "Keep holding to open Private Vault…",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.primary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Data
            item {
                Column(
                    modifier = Modifier
                        .padding(top = 26.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "DATA",
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
                            icon = Icons.Rounded.RestartAlt,
                            title = "Reset app preferences",
                            subtitle = "Clear custom albums, favorites, PIN & dev settings",
                            danger = true,
                            showDivider = false,
                            onClick = { resetOpen = true }
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.WbSunny, contentDescription = null, tint = palette.textMuted, modifier = Modifier.size(14.dp))
                        Icon(Icons.Rounded.Computer, contentDescription = null, tint = palette.textMuted, modifier = Modifier.size(14.dp))
                        Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = palette.textMuted, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = "${uiState.appNameLabel} 1.0.0 · 100% Offline Local Gallery",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = palette.textMuted
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = statusMessage != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(palette.text)
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Text(
                    text = statusMessage.orEmpty(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.bg
                )
            }
        }
    }

    // Secret Developer Code Dialog
    if (codeDialogOpen) {
        AlertDialog(
            onDismissRequest = { codeDialogOpen = false },
            containerColor = palette.surface,
            title = {
                Text(
                    text = "Unlock Developer Options",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.text
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter the secret developer code to unlock advanced controls.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textMuted
                    )
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = {
                            codeInput = it
                            codeError = null
                        },
                        singleLine = true,
                        placeholder = { Text("Enter secret code") },
                        isError = codeError != null,
                        shape = RoundedCornerShape(14.dp),
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
                            .testTag("developer_secret_code_input")
                    )
                    if (codeError != null) {
                        Text(
                            text = codeError.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.danger
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (viewModel.unlockDeveloperOptions(codeInput)) {
                            codeDialogOpen = false
                            statusMessage = "Developer Options unlocked"
                            onOpenDeveloperOptions()
                        } else {
                            codeError = "Invalid secret code"
                        }
                    },
                    modifier = Modifier.testTag("developer_secret_code_confirm")
                ) {
                    Text("Unlock", fontWeight = FontWeight.Bold, color = palette.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { codeDialogOpen = false }) {
                    Text("Cancel", color = palette.textMuted)
                }
            }
        )
    }

    // 4-Digit Hidden Vault PIN Sheet (Setup on first time, Verify thereafter)
    if (pinSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { pinSheetOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = palette.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                val isFirstSetup = !uiState.hasVaultPin
                Text(
                    text = when {
                        isFirstSetup && pinSetupStep == 0 -> "Create Private Vault PIN"
                        isFirstSetup -> "Confirm 4-digit PIN"
                        else -> "Unlock Private Vault"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.text
                )
                Text(
                    text = when {
                        isFirstSetup && pinSetupStep == 0 -> "First time opening the Private Vault. Set a 4-digit PIN stored securely on your device."
                        isFirstSetup -> "Re-enter your 4-digit PIN to save it."
                        else -> "Enter your 4-digit PIN to view hidden photos."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textMuted,
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                )

                val currentVal = if (isFirstSetup && pinSetupStep == 1) pinConfirmInput else pinInput
                OutlinedTextField(
                    value = currentVal,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(4)
                        pinError = null
                        if (isFirstSetup && pinSetupStep == 1) {
                            pinConfirmInput = digits
                        } else {
                            pinInput = digits
                        }
                    },
                    singleLine = true,
                    placeholder = { Text("4-digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = pinError != null,
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
                        .testTag("vault_pin_input")
                )

                if (pinError != null) {
                    Text(
                        text = pinError.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.danger,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.primary)
                        .clickable {
                            if (isFirstSetup) {
                                if (pinSetupStep == 0) {
                                    if (pinInput.length != 4) {
                                        pinError = "Enter exactly 4 digits"
                                    } else {
                                        pinSetupStep = 1
                                    }
                                } else {
                                    if (pinConfirmInput != pinInput) {
                                        pinError = "PINs do not match"
                                        pinConfirmInput = ""
                                    } else {
                                        viewModel.setVaultPin(pinInput)
                                        pinSheetOpen = false
                                        onOpenHiddenVault()
                                    }
                                }
                            } else {
                                if (pinInput.length != 4) {
                                    pinError = "Enter 4 digits"
                                } else if (viewModel.verifyVaultPin(pinInput)) {
                                    pinSheetOpen = false
                                    onOpenHiddenVault()
                                } else {
                                    pinError = "Incorrect PIN"
                                    pinInput = ""
                                }
                            }
                        }
                        .padding(vertical = 14.dp)
                        .testTag("vault_pin_submit"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            isFirstSetup && pinSetupStep == 0 -> "Continue"
                            isFirstSetup -> "Save PIN & Open Vault"
                            else -> "Unlock Vault"
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }

    // Reset Preferences Sheet
    if (resetOpen) {
        ModalBottomSheet(
            onDismissRequest = { resetOpen = false },
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
                    text = "Reset app preferences?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.text
                )
                Text(
                    text = "This clears custom albums, favorites, vault PIN, and Developer Options state. Your device photos stay untouched.",
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
                            .clickable { resetOpen = false }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.text
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.danger)
                            .clickable {
                                viewModel.resetData()
                                resetOpen = false
                                statusMessage = "App preferences reset"
                            }
                            .padding(vertical = 14.dp)
                            .testTag("confirm_reset_library"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
