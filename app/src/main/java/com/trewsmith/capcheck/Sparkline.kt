package com.trewsmith.capcheck

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun Sparkline(data: List<Float>, modifier: Modifier = Modifier) {
    if (data.size < 2) return

    val color = if (data.last() >= data.first()) Color(0xFF4CAF50) else Color.Red

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val max = data.maxOrNull() ?: 0f
        val min = data.minOrNull() ?: 0f
        val range = if (max - min == 0f) 1f else max - min

        val stepX = width / (data.size - 1)

        for (i in 0 until data.size - 1) {
            val startX = i * stepX
            val startY = height - ((data[i] - min) / range * height)
            val endX = (i + 1) * stepX
            val endY = height - ((data[i + 1] - min) / range * height)

            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2f
            )
        }
    }
}
