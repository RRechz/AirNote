package com.babelsoftware.loudly.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedSoundWave(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    barCount: Int = 4,
    barWidth: Dp = 3.dp,
    barMaxHeight: Dp = 20.dp,
    barMinHeight: Dp = 4.dp,
    barSpacing: Dp = 2.dp,
    barColor: Color = Color.White
) {
    val barHeights = remember { List(barCount) { mutableStateOf(barMinHeight) } }

    LaunchedEffect(key1 = isPlaying) {
        if (!isPlaying) {
            barHeights.forEach {
                launch {
                    animate(
                        initialValue = it.value.value,
                        targetValue = barMinHeight.value,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    ) { value, _ ->
                        it.value = value.dp
                    }
                }
            }
            return@LaunchedEffect
        }
        while (true) {
            barHeights.forEachIndexed { index, barHeight ->
                val (delay, targetHeight) = when (index) {
                    0, 3 -> 150L to barMaxHeight * 0.7f
                    1, 2 -> 0L to barMaxHeight
                    else -> 0L to barMinHeight
                }

                launch {
                    delay(delay)
                    animate(
                        initialValue = barHeight.value.value,
                        targetValue = targetHeight.value,
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    ) { value, _ -> barHeight.value = value.dp }

                    animate(
                        initialValue = barHeight.value.value,
                        targetValue = barMinHeight.value,
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    ) { value, _ -> barHeight.value = value.dp }
                }
            }
            delay(1100)
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(barSpacing),
        verticalAlignment = Alignment.Bottom
    ) {
        barHeights.forEach { height ->
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(height.value)
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}