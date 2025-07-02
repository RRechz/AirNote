package com.babelsoftware.airnote.presentation.screens.settings.settings

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.babelsoftware.airnote.constant.SupportConst
import com.babelsoftware.airnote.presentation.screens.settings.SettingsScaffold
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.widgets.ActionType
import com.babelsoftware.airnote.presentation.screens.settings.widgets.SettingsBox
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.presentation.screens.settings.model.IconResource

@Composable
fun SupportScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.cryptocurrency),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = "Bitcoin (BTC)",
                    icon = IconResource.Vector(Icons.Rounded.AccountBalanceWallet),
                    actionType = ActionType.CLIPBOARD,
                    radius = shapeManager(
                        isFirst = true,
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                    clipboardText = SupportConst.BITCOIN_ADDRESS
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = "Proton Wallet",
                    icon = IconResource.Vector(Icons.Rounded.AccountBalanceWallet),
                    actionType = ActionType.CLIPBOARD,
                    radius = shapeManager(
                        isLast = true,
                        radius = settingsViewModel.settings.value.cornerRadius),
                    clipboardText = SupportConst.PROTON_WALLET_ADDRESS
                )
            }
        }
    }

}