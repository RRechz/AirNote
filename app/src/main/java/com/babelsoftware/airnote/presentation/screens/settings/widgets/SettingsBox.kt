/*
 * Copyright (C) 2025 Babel Software
 *
 * The AirNote project is based solely on the resources of Kin69's Easy Notes project.
 * The AirNote project was developed as an alternative to other AI-powered note-taking applications.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.babelsoftware.airnote.presentation.screens.settings.widgets

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.presentation.screens.settings.model.IconResource
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.theme.FontUtils

enum class ActionType {
    RADIOBUTTON,
    SWITCH,
    LINK,
    TEXT,
    CUSTOM,
    CLIPBOARD
}


@Composable
fun SettingsBox(
    radius: RoundedCornerShape? = null,
    title: String,
    description: String? = null,
    icon: IconResource,
    size: Dp = 12.dp,
    isEnabled: Boolean = true,
    isCentered: Boolean = false,
    actionType: ActionType,
    variable: Boolean? = null,
    switchEnabled: (Boolean) -> Unit = {},
    linkClicked: () -> Unit = {},
    customButton: @Composable () -> Unit = { RenderCustomIcon() },
    customAction: @Composable (() -> Unit) -> Unit = {},
    customText: String = "",
    clipboardText: String = "",
    settingsViewModel: SettingsViewModel? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    val context = LocalContext.current
    var showCustomAction by remember { mutableStateOf(false) }
    if (showCustomAction) customAction { showCustomAction = !showCustomAction }

    AnimatedVisibility(visible = isEnabled) {
        Surface(
            modifier = Modifier
                .padding(bottom = dimensionResource(id = R.dimen.card_padding_bottom))
                .clip(radius ?: RoundedCornerShape(13.dp))
                .clickable {
                    handleAction(
                        context,
                        actionType,
                        variable,
                        switchEnabled,
                        { showCustomAction = !showCustomAction },
                        linkClicked,
                        clipboardText
                    )
                },
            color = containerColor,
            tonalElevation = 1.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.card_padding_horizontal),
                        vertical = size
                    )
                    .fillMaxWidth()
            ) {
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    CircleWrapper(
                        size = 12.dp,
                        color = containerColor
                    ) {
                        // Icon according to the type of the incoming ‘icon’ object
                        when (icon) {
                            is IconResource.Vector -> {
                                // If Vector, show normal Icon
                                Icon(
                                    imageVector = icon.imageVector,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            is IconResource.Url -> {
                                // If Url, show Image with Coil
                                Image(
                                    painter = rememberAsyncImagePainter(model = icon.url),
                                    contentDescription = title,
                                    // For CircleWrapper to fill in
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface, // Visible color
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (!description.isNullOrBlank()) {
                            Text(
                                text = description,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, // Visible color
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                RenderActionComponent(actionType, variable, switchEnabled, linkClicked, customText, customButton, settingsViewModel)
            }
        }
    }
}

private fun handleAction(
    context: Context,
    actionType: ActionType,
    variable: Boolean?,
    onSwitchEnabled: (Boolean) -> Unit,
    customAction: () -> Unit,
    onLinkClicked: () -> Unit,
    clipboardText: String
) {
    when (actionType) {
        ActionType.RADIOBUTTON -> onSwitchEnabled(variable == false)
        ActionType.SWITCH -> onSwitchEnabled(variable == false)
        ActionType.LINK -> onLinkClicked()
        ActionType.CUSTOM -> customAction()
        ActionType.CLIPBOARD -> copyToClipboard(context, clipboardText)
        ActionType.TEXT -> { /* No action needed */ }
    }
}

@Composable
private fun RenderClipboardIcon() {
    Icon(
        imageVector = Icons.Default.ContentCopy,
        contentDescription = null,
        modifier = Modifier.padding(dimensionResource(id = R.dimen.icon_padding)),
        tint = MaterialTheme.colorScheme.primary
    )
}

fun copyToClipboard(context: Context, clipboardText: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", clipboardText)
    clipboard.setPrimaryClip(clip)
}

@Composable
private fun RenderActionComponent(
    actionType: ActionType,
    variable: Boolean?,
    onSwitchEnabled: (Boolean) -> Unit,
    onLinkClicked: () -> Unit,
    customText: String,
    customButton: @Composable () -> Unit,
    settingsViewModel: SettingsViewModel? = null
) {
    when (actionType) {
        ActionType.RADIOBUTTON -> RenderRadioButton(variable, onSwitchEnabled)
        ActionType.SWITCH -> RenderSwitch(variable, onSwitchEnabled)
        ActionType.LINK -> RenderLinkIcon(onLinkClicked)
        ActionType.TEXT -> RenderText(customText, settingsViewModel)
        ActionType.CLIPBOARD -> RenderClipboardIcon()
        ActionType.CUSTOM -> customButton()
    }
}

@Composable
private fun RenderRadioButton(variable: Boolean?, onSwitchEnabled: (Boolean) -> Unit) {
    RadioButton(
        selected = variable == true,
        onClick = { onSwitchEnabled(true) }
    )
}

@Composable
private fun RenderSwitch(variable: Boolean?, onSwitchEnabled: (Boolean) -> Unit) {
    Switch(
        checked = variable == true,
        onCheckedChange = { onSwitchEnabled(it) },
        modifier = Modifier
            .scale(0.9f)
            .padding(0.dp)
    )
}

@Composable
private fun RenderLinkIcon(onLinkClicked: () -> Unit) {
    Icon(
        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
        contentDescription = null,
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.icon_padding))
            .clickable { onLinkClicked() },
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun RenderCustomIcon() {
    Icon(
        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
        contentDescription = null,
        modifier = Modifier
            .scale(0.6f)
            .padding(dimensionResource(id = R.dimen.icon_padding))
    )
}

@Composable
private fun RenderText(customText: String, settingsViewModel: SettingsViewModel? = null) {
    Text(
        text = customText,
        fontSize = settingsViewModel?.let {
            FontUtils.getFontSize(it, baseSize = 14)
        } ?: 14.sp,
        modifier = Modifier.padding(dimensionResource(id = R.dimen.icon_padding))
    )
}
