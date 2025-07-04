package com.babelsoftware.airnote.presentation.screens.settings.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.constant.ConnectionConst
import com.babelsoftware.airnote.presentation.screens.settings.SettingsScaffold
import com.babelsoftware.airnote.presentation.screens.settings.model.IconResource
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.widgets.ActionType
import com.babelsoftware.airnote.presentation.screens.settings.widgets.SettingsBox


@Composable
fun AboutScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.about),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.build_type),
                    description = settingsViewModel.build,
                    icon = IconResource.Vector(Icons.Rounded.Build),
                    actionType = ActionType.TEXT,
                    radius = shapeManager(
                        isFirst = true,
                        radius = settingsViewModel.settings.value.cornerRadius
                    )
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.version),
                    description = settingsViewModel.version,
                    icon = IconResource.Vector(Icons.Rounded.Info),
                    actionType = ActionType.TEXT,
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = "AirNote AI Version",
                    description = "v1.0 build v0.9.0",
                    icon = IconResource.Vector(Icons.Rounded.Info),
                    actionType = ActionType.TEXT,
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.developer),
                    description = stringResource(id = R.string.info_dev),
                    icon = IconResource.Url("https://avatars.githubusercontent.com/u/178022701?v=4"),
                    actionType = ActionType.TEXT,
                    radius = shapeManager(
                        isLast = true,
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.email),
                    icon = IconResource.Vector(Icons.Rounded.Email),
                    clipboardText = ConnectionConst.SUPPORT_MAIL,
                    actionType = ActionType.CLIPBOARD,
                    radius = shapeManager(
                        isFirst = true,
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.source_code),
                    icon = IconResource.Vector(Icons.Rounded.Download),
                    actionType = ActionType.LINK,
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                    linkClicked = { uriHandler.openUri("https://github.com/RRechz/AirNote/") }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    size = 8.dp,
                    title = stringResource(id = R.string.feature),
                    icon = IconResource.Vector(Icons.Rounded.BugReport),
                    linkClicked = { uriHandler.openUri(ConnectionConst.FEATURE_REQUEST) },
                    actionType = ActionType.LINK,
                    radius = shapeManager(
                        isLast = true,
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                )
            }
        }
    }
}

@Composable
fun UserCards(uriHandler: UriHandler) {
    Column {
        UserCard(
            imageUrl = "https://avatars.githubusercontent.com/u/178022701?v=4",
            name = "Mustafa Burak Özcan",
            role = stringResource(R.string.info_dev),
            onClick = { uriHandler.openUri("https://github.com/RRechz") }
        )
        // UserCard(
            // imageUrl = "https://avatars.githubusercontent.com/u/196631623?v=4",
            // name = "Rescci",
            // role = stringResource(R.string.info_dev2),
            // onClick = { uriHandler.openUri("https://github.com/RRechz") }
        // )
    }
}

@Composable
fun UserCard(
    imageUrl: String,
    name: String,
    role: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .height(140.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = role,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Decorative element
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp)
                    .offset(x = 20.dp, y = (-20).dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        CircleShape
                    )
            )
        }
    }
}