package com.mikix.ui

import android.view.Choreographer
import android.view.TextureView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.filament.Engine

@Composable
fun PerformanceGated3DX(
    filamentEnabled: Boolean,
    lowPerformanceMode: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val capable = remember(context) { DeviceCapability.supportsRealtime3D(context) }
    if (filamentEnabled && !lowPerformanceMode && capable) {
        FilamentRealtimeXCard(modifier)
    } else {
        RotatingX3DMark()
    }
}

@Composable
private fun FilamentRealtimeXCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val engine = remember { Engine.create() }
    var frameCount by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount++
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        Choreographer.getInstance().postFrameCallback(callback)
        onDispose {
            Choreographer.getInstance().removeFrameCallback(callback)
            engine.destroy()
        }
    }

    val transition = rememberInfiniteTransition(label = "filament-x")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFF0A1427))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(factory = { TextureView(context) }, modifier = Modifier.matchParentSize())
        Text(
            text = "X",
            style = MaterialTheme.typography.displayLarge,
            color = Color(0xFFF5B938),
            modifier = Modifier.rotate(angle)
        )
        Text(
            text = "Filament realtime mode • frame $frameCount",
            color = Color(0x88FFFFFF),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
