@file:Suppress("DEPRECATION")
package com.babelsoftware.loudly.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.NoCell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.ContentCountryKey
import com.babelsoftware.loudly.constants.ContentLanguageKey
import com.babelsoftware.loudly.constants.CountryCodeToName
import com.babelsoftware.loudly.constants.HideExplicitKey
import com.babelsoftware.loudly.constants.LanguageCodeToName
import com.babelsoftware.loudly.constants.LikedAutoDownloadKey
import com.babelsoftware.loudly.constants.LikedAutodownloadMode
import com.babelsoftware.loudly.constants.ProxyEnabledKey
import com.babelsoftware.loudly.constants.ProxyTypeKey
import com.babelsoftware.loudly.constants.ProxyUrlKey
import com.babelsoftware.loudly.constants.SYSTEM_DEFAULT
import com.babelsoftware.loudly.constants.SelectedLanguageKey
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.component.TextFieldDialog
import com.babelsoftware.loudly.ui.screens.settings.card_design.ActionType
import com.babelsoftware.loudly.ui.screens.settings.card_design.IconResource
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingCategory
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.screens.settings.card_design.shapeManager
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.utils.rememberEnumPreference
import com.babelsoftware.loudly.utils.rememberPreference
import java.net.Proxy
import java.util.Locale

@SuppressLint("PrivateResource")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val (likedAutoDownload, onLikedAutoDownload) = rememberEnumPreference(LikedAutoDownloadKey, LikedAutodownloadMode.OFF)
    val (contentLanguage, onContentLanguageChange) = rememberPreference(key = ContentLanguageKey, defaultValue = "system")
    val (contentCountry, onContentCountryChange) = rememberPreference(key = ContentCountryKey, defaultValue = "system")
    val (selectedLanguage, onSelectedLanguage) = rememberPreference(key = SelectedLanguageKey, defaultValue = "system")
    val (hideExplicit, onHideExplicitChange) = rememberPreference(key = HideExplicitKey, defaultValue = false)
    val (proxyEnabled, onProxyEnabledChange) = rememberPreference(key = ProxyEnabledKey, defaultValue = false)
    val (proxyType, onProxyTypeChange) = rememberEnumPreference(key = ProxyTypeKey, defaultValue = Proxy.Type.HTTP)
    val (proxyUrl, onProxyUrlChange) = rememberPreference(key = ProxyUrlKey, defaultValue = "host:port")

    var showContentLanguageDialog by remember { mutableStateOf(false) }
    var showContentCountryDialog by remember { mutableStateOf(false) }
    var showLikeAutodownloadDialog by remember { mutableStateOf(false) }
    var showAppLanguageDialog by remember { mutableStateOf(false) }
    var showProxyTypeDialog by remember { mutableStateOf(false) }
    var showProxyUrlDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.content)) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        if (showContentLanguageDialog) {
            SelectionListDialog(
                title = stringResource(R.string.content_language),
                items = listOf(SYSTEM_DEFAULT) + LanguageCodeToName.keys.toList(),
                itemText = { LanguageCodeToName.getOrElse(it) { stringResource(R.string.system_default) } },
                selectedItem = contentLanguage,
                onItemSelected = {
                    onContentLanguageChange(it)
                    showContentLanguageDialog = false
                },
                onDismissRequest = { showContentLanguageDialog = false }
            )
        }

        if (showContentCountryDialog) {
            SelectionListDialog(
                title = stringResource(R.string.content_country),
                items = listOf(SYSTEM_DEFAULT) + CountryCodeToName.keys.toList(),
                itemText = { CountryCodeToName.getOrElse(it) { stringResource(R.string.system_default) } },
                selectedItem = contentCountry,
                onItemSelected = {
                    onContentCountryChange(it)
                    showContentCountryDialog = false
                },
                onDismissRequest = { showContentCountryDialog = false }
            )
        }

        if (showLikeAutodownloadDialog) {
            SelectionListDialog(
                title = stringResource(R.string.like_autodownload),
                items = LikedAutodownloadMode.values().toList(),
                itemText = {
                    when (it) {
                        LikedAutodownloadMode.OFF -> stringResource(R.string.state_off)
                        LikedAutodownloadMode.ON -> stringResource(R.string.state_on)
                        LikedAutodownloadMode.WIFI_ONLY -> stringResource(R.string.wifi_only)
                    }
                },
                selectedItem = likedAutoDownload,
                onItemSelected = {
                    onLikedAutoDownload(it)
                    showLikeAutodownloadDialog = false
                },
                onDismissRequest = { showLikeAutodownloadDialog = false }
            )
        }

        if (showAppLanguageDialog) {
            SelectionListDialog(
                title = stringResource(R.string.app_language),
                items = listOf(SYSTEM_DEFAULT) + LanguageCodeToName.keys.toList(),
                itemText = { LanguageCodeToName[it] ?: stringResource(R.string.system_default) },
                selectedItem = selectedLanguage,
                onItemSelected = {
                    onSelectedLanguage(it)
                    updateLanguage(context, it)
                    saveLanguagePreference(context, it)
                    showAppLanguageDialog = false
                },
                onDismissRequest = { showAppLanguageDialog = false }
            )
        }

        if (showProxyTypeDialog) {
            SelectionListDialog(
                title = stringResource(R.string.proxy_type),
                items = listOf(Proxy.Type.HTTP, Proxy.Type.SOCKS),
                itemText = { it.name },
                selectedItem = proxyType,
                onItemSelected = {
                    onProxyTypeChange(it)
                    showProxyTypeDialog = false
                },
                onDismissRequest = { showProxyTypeDialog = false }
            )
        }

        if (showProxyUrlDialog) {
            TextFieldDialog(
                initialTextFieldValue = TextFieldValue(proxyUrl),
                onDone = {
                    onProxyUrlChange(it)
                    showProxyUrlDialog = false
                },
                onDismiss = { showProxyUrlDialog = false },
                isInputValid = { it.contains(":") }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            SettingCategory(title = stringResource(R.string.content))
            SettingsBox(
                title = stringResource(R.string.content_language),
                description = LanguageCodeToName.getOrElse(contentLanguage) { stringResource(R.string.system_default) },
                icon = IconResource.Drawable(painterResource(R.drawable.language)),
                shape = shapeManager(isFirst = true),
                onClick = { showContentLanguageDialog = true }
            )
            SettingsBox(
                title = stringResource(R.string.content_country),
                description = CountryCodeToName.getOrElse(contentCountry) { stringResource(R.string.system_default) },
                icon = IconResource.Drawable(painterResource(R.drawable.location_on)),
                shape = shapeManager(),
                onClick = { showContentCountryDialog = true }
            )
            SettingsBox(
                title = stringResource(R.string.hide_explicit),
                icon = IconResource.Drawable(painterResource(R.drawable.explicit)),
                actionType = ActionType.SWITCH,
                isChecked = hideExplicit,
                onCheckedChange = onHideExplicitChange,
                shape = shapeManager()
            )
            SettingsBox(
                title = stringResource(R.string.like_autodownload),
                description = when (likedAutoDownload) {
                    LikedAutodownloadMode.OFF -> stringResource(R.string.state_off)
                    LikedAutodownloadMode.ON -> stringResource(R.string.state_on)
                    LikedAutodownloadMode.WIFI_ONLY -> stringResource(R.string.wifi_only)
                },
                icon = IconResource.Vector(Icons.Rounded.Favorite),
                shape = shapeManager(isLast = true),
                onClick = { showLikeAutodownloadDialog = true }
            )

            SettingCategory(title = stringResource(R.string.system_integration))
            SettingsBox(
                title = stringResource(R.string.keep_to_alive_settings),
                icon = IconResource.Vector(Icons.Rounded.NoCell),
                shape = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) shapeManager(isFirst = true) else shapeManager(isBoth = true),
                onClick = { navController.navigate("settings/content/notification") }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SettingsBox(
                    title = stringResource(R.string.open_supported_links),
                    description = stringResource(R.string.configure_supported_links),
                    icon = IconResource.Drawable(painterResource(R.drawable.add_link)),
                    shape = shapeManager(isLast = true),
                    onClick = {
                        try {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, "package:${context.packageName}".toUri()),
                            )
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, R.string.intent_supported_links_not_found, Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }

            SettingCategory(title = stringResource(R.string.app_language))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                SettingsBox(
                    title = stringResource(R.string.app_language),
                    icon = IconResource.Drawable(painterResource(R.drawable.translate)),
                    shape = shapeManager(isBoth = true),
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_APP_LOCALE_SETTINGS, "package:${context.packageName}".toUri()))
                    }
                )
            } else {
                SettingsBox(
                    title = stringResource(R.string.app_language),
                    description = LanguageCodeToName[selectedLanguage] ?: stringResource(R.string.system_default),
                    icon = IconResource.Drawable(painterResource(R.drawable.translate)),
                    shape = shapeManager(isBoth = true),
                    onClick = { showAppLanguageDialog = true }
                )
            }

            SettingCategory(title = stringResource(R.string.proxy))
            SettingsBox(
                title = stringResource(R.string.enable_proxy),
                icon = IconResource.Drawable(painterResource(R.drawable.wifi_proxy)),
                actionType = ActionType.SWITCH,
                isChecked = proxyEnabled,
                onCheckedChange = onProxyEnabledChange,
                shape = if (proxyEnabled) shapeManager(isFirst = true) else shapeManager(isBoth = true)
            )

            AnimatedVisibility(proxyEnabled) {
                Column {
                    SettingsBox(
                        title = stringResource(R.string.proxy_type),
                        description = proxyType.name,
                        shape = shapeManager(),
                        onClick = { showProxyTypeDialog = true }
                    )
                    SettingsBox(
                        title = stringResource(R.string.proxy_url),
                        description = proxyUrl,
                        shape = shapeManager(isLast = true),
                        onClick = { showProxyUrlDialog = true }
                    )
                }
            }
        }
    }
}

/**
 * A reusable selection dialog for use only within this file.
 */
@Composable
private fun <T> SelectionListDialog(
    title: String,
    items: List<T>,
    itemText: @Composable (T) -> String,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = title) },
        text = {
            LazyColumn {
                items(items) { item ->
                    val isSelected = item == selectedItem
                    Text(
                        text = itemText(item),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(item) }
                            .padding(vertical = 14.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

fun saveLanguagePreference(context: Context, languageCode: String) {
    val sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    sharedPreferences.edit { putString("app_language", languageCode) }
}

fun updateLanguage(context: Context, languageCode: String) {
    val locale = if (languageCode == "system") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    } else {
        Locale(languageCode)
    }

    val config = Configuration(context.resources.configuration)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocales(LocaleList(locale))
    } else {
        @Suppress("DEPRECATION")
        config.setLocale(locale)
    }

    @Suppress("DEPRECATION")
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}