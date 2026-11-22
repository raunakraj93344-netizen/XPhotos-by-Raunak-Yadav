package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GalleryViewModel
import com.example.ui.XPhotosRoot
import com.example.ui.components.DebugHudOverlay
import com.example.ui.theme.XPhotosTheme
import com.example.ui.theme.xPalette

class MainActivity : ComponentActivity() {

    private val viewModel: GalleryViewModel by viewModels {
        GalleryViewModel.Factory((application as XPhotosApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val dev = uiState.developerSettings

            XPhotosTheme(
                themeMode = uiState.settings.themeMode,
                accentPreset = dev.accentPreset,
                pureAmoledDark = dev.pureAmoledDark,
                animationSpeedScale = dev.animationSpeedScale
            ) {
                val isDark = MaterialTheme.xPalette.isDark
                SideEffect {
                    val controller = WindowCompat.getInsetsController(window, window.decorView)
                    controller.isAppearanceLightStatusBars = !isDark
                    controller.isAppearanceLightNavigationBars = !isDark
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    XPhotosRoot(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                    if (dev.unlocked && dev.showFpsRamHud) {
                        DebugHudOverlay(
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDevicePhotos()
    }
}
