package com.example.ui.components

import android.app.ActivityManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive

@Composable
fun DebugHudOverlay(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fps by remember { mutableIntStateOf(60) }
    var heapUsedMb by remember { mutableLongStateOf(0L) }
    var sysAvailMb by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        var frames = 0
        var windowStartNanos = 0L

        while (isActive) {
            withFrameNanos { frameTime ->
                if (windowStartNanos == 0L) {
                    windowStartNanos = frameTime
                }
                frames++
                val elapsed = frameTime - windowStartNanos
                if (elapsed >= 500_000_000L) {
                    val computed = ((frames * 1_000_000_000.0) / elapsed).toInt().coerceIn(1, 144)
                    fps = computed
                    frames = 0
                    windowStartNanos = frameTime

                    val rt = Runtime.getRuntime()
                    heapUsedMb = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L)
                    if (am != null) {
                        am.getMemoryInfo(memInfo)
                        sysAvailMb = memInfo.availMem / (1024L * 1024L)
                    }
                }
            }
        }
    }

    val fpsColor = when {
        fps >= 50 -> Color(0xFF3DDC97)
        fps >= 30 -> Color(0xFFFFC24B)
        else -> Color(0xFFFF5D5D)
    }

    Row(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 6.dp, end = 12.dp)
            .clip(CircleShape)
            .background(Color(0xE00B0B14))
            .border(0.75.dp, Color.White.copy(alpha = 0.16f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(fpsColor)
        )
        Text(
            text = "${fps} FPS · ${heapUsedMb}MB Heap · ${sysAvailMb}MB Free",
            color = Color.White,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
