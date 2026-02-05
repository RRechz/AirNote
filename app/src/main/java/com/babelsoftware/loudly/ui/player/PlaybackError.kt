package com.babelsoftware.loudly.ui.player

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.PlaybackException
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.DarkModeKey
import com.babelsoftware.loudly.constants.PlayerBackgroundStyle
import com.babelsoftware.loudly.constants.PlayerBackgroundStyleKey
import com.babelsoftware.loudly.ui.screens.settings.DarkMode
import com.babelsoftware.loudly.utils.rememberEnumPreference

@Composable
fun PlaybackError(
    error: PlaybackException,
    retry: () -> Unit,
) {
    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.DEFAULT
    )
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }
    val textColor = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.secondary
        else -> if (useDarkTheme) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
    }

    val (errorMessage, errorDescription) = remember(error.errorCode) {
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                Pair(
                    R.string.error_network_title,
                    R.string.error_network_description
                )
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
            PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED ->
                Pair(
                    R.string.error_unsupported_format_title,
                    R.string.error_unsupported_format_description
                )
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW ->
                Pair(
                    R.string.error_behind_live_window_title,
                    R.string.error_behind_live_window_description
                )
            else -> {
                Pair(
                    R.string.error_playback_failed_title,
                    R.string.error_playback_failed_description
                )
            }
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { retry() })
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.info),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )

        Column {
            Text(
                text = stringResource(errorMessage),
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = errorDescription),
                color = textColor.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp
            )
        }
    }
}