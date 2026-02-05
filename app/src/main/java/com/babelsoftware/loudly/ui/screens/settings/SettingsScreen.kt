package com.babelsoftware.loudly.ui.screens.settings

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.babelsoftware.innertube.utils.parseCookieString
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.AccountNameKey
import com.babelsoftware.loudly.constants.HeaderCardAlignmentKey
import com.babelsoftware.loudly.constants.HeaderCardCornerRadiusKey
import com.babelsoftware.loudly.constants.HeaderCardGradientIntensityKey
import com.babelsoftware.loudly.constants.HeaderCardProfilePictureUriKey
import com.babelsoftware.loudly.constants.HeaderImageKey
import com.babelsoftware.loudly.constants.InnerTubeCookieKey
import com.babelsoftware.loudly.constants.ShowHeaderCardTextKey
import com.babelsoftware.loudly.ui.screens.settings.card_design.IconResource
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingCategory
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.screens.settings.card_design.shapeManager
import com.babelsoftware.loudly.ui.screens.settings.viewmodel.SettingsViewModel
import com.babelsoftware.loudly.utils.rememberEnumPreference
import com.babelsoftware.loudly.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val updateAvailable by settingsViewModel.updateAvailable

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(painterResource(R.drawable.arrow_back), stringResource(R.string.back))
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
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            AccountHeaderCard()

            SettingCategory(title = stringResource(R.string.category_interface))
            SettingsBox(
                title = stringResource(R.string.appearance),
                description = stringResource(R.string.appearance_description),
                icon = IconResource.Drawable(painterResource(R.drawable.palette)),
                shape = shapeManager(isBoth = true),
                onClick = { navController.navigate("settings/appearance") }
            )

            SettingCategory(title = stringResource(R.string.category_content))
            SettingsBox(
                title = stringResource(R.string.account),
                description = stringResource(R.string.account_description),
                icon = IconResource.Drawable(painterResource(R.drawable.person)),
                shape = shapeManager(isFirst = true),
                onClick = { navController.navigate("settings/account") }
            )
            SettingsBox(
                title = stringResource(R.string.content),
                description = stringResource(R.string.content_description),
                icon = IconResource.Drawable(painterResource(R.drawable.language)),
                shape = shapeManager(isLast = true),
                onClick = { navController.navigate("settings/content") }
            )

            SettingCategory(title = stringResource(R.string.category_player))
            SettingsBox(
                title = stringResource(R.string.player_and_audio),
                description = stringResource(R.string.player_and_audio_description),
                icon = IconResource.Drawable(painterResource(R.drawable.play)),
                shape = shapeManager(isBoth = true),
                onClick = { navController.navigate("settings/player") }
            )

            SettingCategory(title = stringResource(R.string.category_system))
            SettingsBox(
                title = stringResource(R.string.tips_and_features),
                description = stringResource(R.string.tips_and_fetures_description),
                icon = IconResource.Drawable(painterResource(R.drawable.tips)),
                shape = shapeManager(isFirst = true),
                onClick = { navController.navigate("settings/tips") }
            )
            SettingsBox(
                title = stringResource(R.string.ComradeNotification),
                description = stringResource(R.string.comrade_notification_description),
                icon = IconResource.Drawable(painterResource(R.drawable.ic_smart_hub)),
                shape = shapeManager(),
                onClick = { navController.navigate("settings/comradenotification") }
            )
            SettingsBox(
                title = stringResource(R.string.storage),
                description = stringResource(R.string.storage_description),
                icon = IconResource.Drawable(painterResource(R.drawable.storage)),
                shape = shapeManager(),
                onClick = { navController.navigate("settings/storage") }
            )
            SettingsBox(
                title = stringResource(R.string.privacy),
                description = stringResource(R.string.privacy_description),
                icon = IconResource.Drawable(painterResource(R.drawable.security)),
                // Ortadaki eleman olduğu için şekil parametresi yok (düz köşeler)
                shape = shapeManager(),
                onClick = { navController.navigate("settings/privacy") }
            )
            SettingsBox(
                title = stringResource(R.string.backup_restore),
                description = stringResource(R.string.backup_restore_description),
                icon = IconResource.Drawable(painterResource(R.drawable.restore)),
                shape = shapeManager(),
                onClick = { navController.navigate("settings/backup_restore") }
            )

            // Hakkında öğesinin alt başlığını dinamik yap
            val aboutSubtitle = if (updateAvailable) {
                stringResource(R.string.new_version_available)
            } else {
                stringResource(R.string.about_subtitle)
            }
            SettingsBox(
                title = stringResource(R.string.about),
                description = aboutSubtitle,
                icon = IconResource.Drawable(painterResource(R.drawable.info)),
                // Grubun son öğesi
                shape = shapeManager(isLast = true),
                onClick = { navController.navigate("settings/about") }
            )

            // Alt boşluk
            Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)))
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AccountHeaderCard() {
    val accountName by rememberPreference(AccountNameKey, "")
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }
    val (headerImageKey, _) = rememberPreference(HeaderImageKey, "Loudly-1")
    val (showHeaderCardText, _) = rememberPreference(ShowHeaderCardTextKey, true)
    val (headerCardAlignment, _) = rememberEnumPreference(HeaderCardAlignmentKey, HeaderCardContentAlignment.BottomStart)
    val (gradientIntensity, _) = rememberPreference(HeaderCardGradientIntensityKey, 0.6f)
    val (headerCardCornerRadius, _) = rememberPreference(HeaderCardCornerRadiusKey, 24)
    val (profilePictureUri, _) = rememberPreference(HeaderCardProfilePictureUriKey, "")

    val painter = when {
        headerImageKey.startsWith("content://") -> {
            rememberAsyncImagePainter(model = Uri.parse(headerImageKey))
        }
        headerImageKey == "Loudly-1" -> painterResource(id = R.drawable.loudly_picutre_1)
        headerImageKey == "Loudly-2" -> painterResource(id = R.drawable.loudly_picutre_2)
        headerImageKey == "Loudly-3" -> painterResource(id = R.drawable.loudly_picutre_3)
        headerImageKey == "Loudly-4" -> painterResource(id = R.drawable.loudly_picutre_4)
        headerImageKey == "Loudly-5" -> painterResource(id = R.drawable.loudly_picutre_5)
        headerImageKey == "Loudly-6" -> painterResource(id = R.drawable.loudly_picutre_6)
        else -> painterResource(id = R.drawable.loudly_picutre_1) // default
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(headerCardCornerRadius.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = headerCardAlignment.alignment
        ) {
            Image(
                painter = painter,
                contentDescription = "Header Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.5f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = gradientIntensity)
                            )
                        )
                    )
            )
            if (showHeaderCardText) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (profilePictureUri.isNotBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = Uri.parse(profilePictureUri)),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column(
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        if (isLoggedIn) {
                            Text(
                                stringResource(R.string.Hi),
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                accountName.replace("@", ""),
                                color = Color.White,
                                fontSize = 22.sp,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                stringResource(R.string.not_logged_in),
                                color = Color.White,
                                fontSize = 22.sp,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}