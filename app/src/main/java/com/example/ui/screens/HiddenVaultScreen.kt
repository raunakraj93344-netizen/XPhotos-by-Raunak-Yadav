package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeleteOutcome
import com.example.ui.GalleryUiState
import com.example.ui.GalleryViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PhotoCard
import com.example.ui.components.XPhotosIconButton
import com.example.ui.theme.xPalette

@Composable
fun HiddenVaultScreen(
    uiState: GalleryUiState,
    viewModel: GalleryViewModel,
    onBack: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.xPalette
    val hidden = uiState.hiddenPhotos
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    val selectionMode = selectedIds.isNotEmpty()

    val deleteSenderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onDeviceDeleteConfirmed(result.resultCode == Activity.RESULT_OK)
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
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            XPhotosIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
                modifier = Modifier.testTag("vault_back")
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PIN PROTECTED",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = palette.primary
                )
                Text(
                    text = if (selectionMode) "${selectedIds.size} selected" else "Private Vault",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.text
                )
            }
            if (selectionMode) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(palette.primarySoft)
                        .clickable {
                            viewModel.unhidePhotos(selectedIds.toList())
                            selectedIds = emptySet()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Rounded.Visibility, contentDescription = null, tint = palette.primary, modifier = Modifier.size(15.dp))
                    Text("Unhide", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = palette.primary)
                }
                XPhotosIconButton(
                    icon = Icons.Rounded.Delete,
                    contentDescription = "Delete selected",
                    tint = palette.danger,
                    onClick = {
                        val chosen = hidden.filter { it.id in selectedIds }
                        selectedIds = emptySet()
                        viewModel.requestDeletePhotos(chosen) { outcome ->
                            if (outcome is DeleteOutcome.RequiresConfirmation) {
                                deleteSenderLauncher.launch(
                                    IntentSenderRequest.Builder(outcome.intentSender).build()
                                )
                            }
                        }
                    }
                )
            } else if (hidden.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(palette.surfaceAlt)
                        .border(0.75.dp, palette.border, CircleShape)
                        .clickable {
                            viewModel.unhidePhotos(hidden.map { it.id })
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Rounded.Visibility, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(14.dp))
                    Text("Unhide all", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = palette.text)
                }
            }
        }

        if (hidden.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Rounded.Lock,
                    title = "Private Vault is empty",
                    message = "Hide any photo from the Photo Viewer or by long-pressing photos in Library. Hidden photos disappear from Library, Albums, and Search.",
                    actionLabel = "Back to Settings",
                    onAction = onBack
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(hidden, key = { it.id }) { photo ->
                    PhotoCard(
                        photo = photo,
                        onClick = {
                            if (selectionMode) {
                                selectedIds = if (photo.id in selectedIds) selectedIds - photo.id else selectedIds + photo.id
                            } else {
                                onOpenPhoto(photo.id)
                            }
                        },
                        onLongClick = {
                            selectedIds = if (photo.id in selectedIds) selectedIds - photo.id else selectedIds + photo.id
                        },
                        aspectRatio = 1f,
                        selectionMode = selectionMode,
                        selected = photo.id in selectedIds,
                        showLabel = false,
                        cornerRadius = 12.dp
                    )
                }
            }
        }
    }
}
