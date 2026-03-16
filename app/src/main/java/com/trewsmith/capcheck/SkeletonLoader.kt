package com.trewsmith.capcheck

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonItem(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .alpha(alpha.value)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray)
    )
}

@Composable
fun SearchSkeleton() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        repeat(5) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                SkeletonItem(modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    SkeletonItem(modifier = Modifier.width(100.dp).height(20.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    SkeletonItem(modifier = Modifier.width(200.dp).height(15.dp))
                }
            }
        }
    }
}

@Composable
fun PortfolioSkeleton() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SkeletonItem(modifier = Modifier.fillMaxWidth().height(150.dp))
        Spacer(modifier = Modifier.height(24.dp))
        repeat(3) {
            SkeletonItem(modifier = Modifier.fillMaxWidth().height(80.dp).padding(vertical = 8.dp))
        }
    }
}
