package com.babelsoftware.loudly.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FrostedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.60f)
    )
    val cardBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f))

    if (onClick != null) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(24.dp),
            colors = cardColors,
            border = cardBorder,
            onClick = onClick,
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(24.dp),
            colors = cardColors,
            border = cardBorder,
        ) {
            content()
        }
    }
}

object AppSpacing {
    val Default = 16.dp
    val Small = 8.dp
    val Large = 24.dp
}