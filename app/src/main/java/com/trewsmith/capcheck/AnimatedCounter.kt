package com.trewsmith.capcheck

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AnimatedCounter(
    targetValue: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) {
    var startValue by remember { mutableStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "CounterAnimation"
    )

    Text(
        text = FormatUtils.formatPrice(animatedValue.toDouble()),
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight
    )
}
