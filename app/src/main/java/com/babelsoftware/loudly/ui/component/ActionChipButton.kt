package com.babelsoftware.loudly.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActionChipButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: String? = null,
    modifier: Modifier = Modifier
) {
    val frostedCardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )
    val frostedCardBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = frostedCardColors,
        border = frostedCardBorder,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Box(modifier = Modifier.size(20.dp)) {
                icon()
            }
            if (text != null) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    modifier = Modifier
                        .widthIn(max = 80.dp)
                        .basicMarquee()
                )
            }
        }
    }
}