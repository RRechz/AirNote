package com.babelsoftware.loudly.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babelsoftware.innertube.YouTube
import com.babelsoftware.innertube.utils.parseCookieString
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.AccountChannelHandleKey
import com.babelsoftware.loudly.constants.AccountEmailKey
import com.babelsoftware.loudly.constants.AccountNameKey
import com.babelsoftware.loudly.constants.InnerTubeCookieKey
import com.babelsoftware.loudly.constants.UseLoginForBrowse
import com.babelsoftware.loudly.constants.YtmSyncKey
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.component.InfoLabel
import com.babelsoftware.loudly.ui.component.TextFieldDialog
import com.babelsoftware.loudly.ui.screens.settings.card_design.ActionType
import com.babelsoftware.loudly.ui.screens.settings.card_design.IconResource
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingCategory
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.screens.settings.card_design.shapeManager
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val accountName by rememberPreference(AccountNameKey, "")
    val accountEmail by rememberPreference(AccountEmailKey, "")
    val accountChannelHandle by rememberPreference(AccountChannelHandleKey, "")
    val (innerTubeCookie, onInnerTubeCookieChange) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }
    val (ytmSync, onYtmSyncChange) = rememberPreference(YtmSyncKey, defaultValue = true)
    val (useLoginForBrowse, onUseLoginForBrowseChange) = rememberPreference(key = UseLoginForBrowse, defaultValue = false)

    var showTokenEditor by remember { mutableStateOf(false) }

    if (showTokenEditor) {
        TextFieldDialog(
            modifier = Modifier,
            initialTextFieldValue = TextFieldValue(innerTubeCookie),
            onDone = { onInnerTubeCookieChange(it) },
            onDismiss = { showTokenEditor = false },
            singleLine = false,
            maxLines = 20,
            isInputValid = {
                it.isNotEmpty() &&
                        try {
                            "SAPISID" in parseCookieString(it)
                            true
                        } catch (e: Exception) {
                            false
                        }
            },
            extraContent = {
                InfoLabel(text = stringResource(R.string.token_adv_login_description))
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account)) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- AACCOUNT GROUP ---
            SettingCategory(title = stringResource(R.string.account))
            val accountDescription = if (isLoggedIn) {
                accountEmail.takeIf { it.isNotEmpty() } ?: accountChannelHandle.takeIf { it.isNotEmpty() }
            } else {
                stringResource(R.string.login_description)
            }

            SettingsBox(
                title = if (isLoggedIn) accountName else stringResource(R.string.login),
                description = accountDescription,
                icon = IconResource.Drawable(painterResource(R.drawable.person)),
                shape = if (isLoggedIn) shapeManager(isFirst = true) else shapeManager(isBoth = true),
                onClick = { if (!isLoggedIn) navController.navigate("login") }
            )

            if (isLoggedIn) {
                SettingsBox(
                    title = stringResource(R.string.token_shown),
                    description = stringResource(R.string.token_adv_login_description),
                    icon = IconResource.Drawable(painterResource(R.drawable.token)),
                    shape = shapeManager(),
                    onClick = { showTokenEditor = true }
                )
                SettingsBox(
                    title = stringResource(R.string.ytm_sync),
                    description = stringResource(R.string.ytm_sync_description),
                    icon = IconResource.Drawable(painterResource(R.drawable.cached)),
                    actionType = ActionType.SWITCH,
                    isChecked = ytmSync,
                    onCheckedChange = onYtmSyncChange,
                    shape = shapeManager()
                )
                SettingsBox(
                    title = stringResource(R.string.logout),
                    icon = IconResource.Drawable(painterResource(R.drawable.logout)),
                    shape = shapeManager(isLast = true),
                    onClick = { onInnerTubeCookieChange("") }
                )
            }

            // --- INTEGRATİONS GROUP ---
            SettingCategory(title = stringResource(R.string.integrations))
            SettingsBox(
                title = stringResource(R.string.import_from_spotify),
                icon = IconResource.Drawable(painterResource(R.drawable.spotify)),
                shape = shapeManager(isFirst = true),
                onClick = { navController.navigate("settings/import_from_spotify/ImportFromSpotify") }
            )
            SettingsBox(
                title = stringResource(R.string.discord_integration),
                icon = IconResource.Drawable(painterResource(R.drawable.discord)),
                shape = shapeManager(isLast = true),
                onClick = { navController.navigate("settings/discord") }
            )

            // --- MISC GROUP ---
            SettingCategory(title = stringResource(R.string.misc))
            SettingsBox(
                title = stringResource(R.string.use_login_for_browse),
                description = stringResource(R.string.use_login_for_browse_desc),
                icon = IconResource.Drawable(painterResource(R.drawable.person)),
                actionType = ActionType.SWITCH,
                isChecked = useLoginForBrowse,
                onCheckedChange = {
                    YouTube.useLoginForBrowse = it
                    onUseLoginForBrowseChange(it)
                },
                shape = shapeManager(isBoth = true)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}