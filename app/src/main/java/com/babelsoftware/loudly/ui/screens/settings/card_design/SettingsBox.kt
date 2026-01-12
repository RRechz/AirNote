package com.babelsoftware.loudly.ui.screens.settings.card_design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

sealed class IconResource {
    data class Drawable(val painter: Painter) : IconResource()
    data class Vector(val imageVector: androidx.compose.ui.graphics.vector.ImageVector) : IconResource()
}

enum class ActionType {
    NAVIGATION,
    TEXT,
    SWITCH
}

@Composable
fun SettingsBox(
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    icon: IconResource? = null,
    actionType: ActionType = ActionType.NAVIGATION,
    actionText: String = "",
    shape: Shape = RoundedCornerShape(24.dp),
    isChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    val finalOnClick = onClick ?: {
        if (actionType == ActionType.SWITCH) {
            onCheckedChange(!isChecked)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (onClick != null || actionType == ActionType.SWITCH) Modifier.clickable(onClick = finalOnClick) else Modifier),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        if (content != null) {
            content()
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        when (icon) {
                            is IconResource.Drawable -> Icon(
                                painter = icon.painter,
                                contentDescription = title,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            is IconResource.Vector -> Icon(
                                imageVector = icon.imageVector,
                                contentDescription = title,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    if (title != null) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (!description.isNullOrBlank()) {
                        if (title != null) Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                when (actionType) {
                    ActionType.NAVIGATION -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.scale(0.8f),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ActionType.TEXT -> {
                        Text(
                            text = actionText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    ActionType.SWITCH -> {
                        val iconVector = if (isChecked) Icons.Default.Check else Icons.Default.Close
                        val iconSize = if (isChecked) 18.dp else 12.dp

                        Switch(
                            checked = isChecked,
                            onCheckedChange = onCheckedChange,
                            modifier = Modifier.scale(0.9f),
                            thumbContent = {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    modifier = Modifier.size(iconSize)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}