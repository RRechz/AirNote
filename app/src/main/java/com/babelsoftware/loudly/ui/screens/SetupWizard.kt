package com.babelsoftware.loudly.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MusicVideo
import androidx.compose.material.icons.rounded.NotInterested
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babelsoftware.innertube.utils.parseCookieString
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.AudioQuality
import com.babelsoftware.loudly.constants.AudioQualityKey
import com.babelsoftware.loudly.constants.DynamicThemeKey
import com.babelsoftware.loudly.constants.FirstSetupPassed
import com.babelsoftware.loudly.constants.InnerTubeCookieKey
import com.babelsoftware.loudly.constants.MiniPlayerAction
import com.babelsoftware.loudly.constants.MiniPlayerActionButtonKey
import com.babelsoftware.loudly.constants.SkipSilenceKey
import com.babelsoftware.loudly.constants.SliderStyle
import com.babelsoftware.loudly.constants.SliderStyleKey
import com.babelsoftware.loudly.constants.StopMusicOnTaskClearKey
import com.babelsoftware.loudly.constants.UseLoginForBrowse
import com.babelsoftware.loudly.utils.rememberEnumPreference
import com.babelsoftware.loudly.utils.rememberPreference

@Composable
fun SetupWizard(
    navController: NavController,
) {
    val (firstSetupPassed, onFirstSetupPassedChange) = rememberPreference(
        FirstSetupPassed,
        defaultValue = false
    )

    var position by remember { mutableIntStateOf(0) }
    val maxPosition = 4 // 0: Welcome, 1: Appearance, 2: Player, 3: Account, 4: Finish

    if (position > 0) {
        BackHandler {
            position--
        }
    }

    if (firstSetupPassed) {
        navController.navigateUp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = painterResource(id = R.drawable.loudly_monochrome),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(40.dp)
                .alpha(0.5f)
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                WizardBottomBar(
                    position = position,
                    maxPosition = maxPosition,
                    onBack = { if (position > 0) position-- },
                    onNext = { if (position < maxPosition) position++ },
                    onFinish = {
                        onFirstSetupPassedChange(true)
                        navController.navigateUp()
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AnimatedContent(
                    targetState = position,
                    label = "WizardStep",
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn()) togetherWith (slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut())
                        } else {
                            (slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn()) togetherWith (slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut())
                        }
                    }
                ) { targetPosition ->
                    when (targetPosition) {
                        0 -> WelcomeStep()
                        1 -> AppearanceSettingsStep()
                        2 -> PlayerSettingsStep()
                        3 -> AccountSettingsStep(navController)
                        4 -> FinishStep()
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation))
        ) {
            Image(
                painter = painterResource(R.drawable.loudly_monochrome),
                contentDescription = "Logo",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground, BlendMode.SrcIn),
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = stringResource(R.string.welcome_to_loudly),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.welcome_to_loudly_description),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureChip(icon = Icons.Rounded.MusicVideo, text = stringResource(R.string.yt_music_fingertips))
            FeatureChip(icon = Icons.Rounded.NotInterested, text = stringResource(R.string.ad_free_playback))
            FeatureChip(icon = Icons.Rounded.Sync, text = stringResource(R.string.ytm_account_sync))
            FeatureChip(icon = Icons.Rounded.OfflinePin, text = stringResource(R.string.offline_music_experience))
            FeatureChip(icon = Icons.Rounded.DirectionsCar, text = stringResource(R.string.android_auto))
            FeatureChip(painterIcon = R.drawable.discord, text = stringResource(R.string.discord_support))
            FeatureChip(painterIcon = R.drawable.github, text = stringResource(R.string.fos_info))
        }
    }
}

@Composable
fun AppearanceSettingsStep() {
    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(DynamicThemeKey, defaultValue = true)
    val (sliderStyle, onSliderStyleChange) = rememberEnumPreference(SliderStyleKey, defaultValue = SliderStyle.COMPOSE)
    val (miniPlayerAction, onMiniPlayerActionChange) = rememberEnumPreference(MiniPlayerActionButtonKey, defaultValue = MiniPlayerAction.Like)

    StepLayout(
        title = stringResource(R.string.appearance),
        description = stringResource(R.string.appearance_setup_desc)
    ) {
        // Dynamic Theme
        WizardSettingsRow(
            title = stringResource(R.string.enable_dynamic_theme),
            description = stringResource(R.string.dynamic_theme_setup_dec),
            icon = painterResource(R.drawable.palette)
        ) {
            Switch(checked = dynamicTheme, onCheckedChange = onDynamicThemeChange)
        }

        // Slider Style
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.slider_style), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SliderStyle.values().forEach { style ->
                val isSelected = sliderStyle == style
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                    onClick = { onSliderStyleChange(style) }
                ) {
                    Text(
                        text = style.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Mini Player Actions
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.mini_player_action_button), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Column(modifier = Modifier.padding(top = 8.dp)) {
            MiniPlayerAction.values().forEach { action ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onMiniPlayerActionChange(action) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = miniPlayerAction == action,
                        onClick = { onMiniPlayerActionChange(action) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = when (action) {
                        MiniPlayerAction.Like -> stringResource(R.string.like)
                        MiniPlayerAction.Next -> stringResource(R.string.next)
                        MiniPlayerAction.Previous -> stringResource(R.string.previous)
                        MiniPlayerAction.Download -> stringResource(R.string.download)
                        MiniPlayerAction.None -> stringResource(R.string.none)
                    })
                    Spacer(modifier = Modifier.weight(1f))
                    // Preview
                    Icon(
                        imageVector = when(action) {
                            MiniPlayerAction.Like -> Icons.Rounded.Favorite
                            MiniPlayerAction.Next -> Icons.Rounded.FastForward
                            MiniPlayerAction.Previous -> Icons.Rounded.FastRewind
                            MiniPlayerAction.Download -> Icons.Rounded.SystemUpdateAlt
                            MiniPlayerAction.None -> Icons.Rounded.NotInterested
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerSettingsStep() {
    val (audioQuality, onAudioQualityChange) = rememberEnumPreference(AudioQualityKey, defaultValue = AudioQuality.AUTO)
    val (skipSilence, onSkipSilenceChange) = rememberPreference(SkipSilenceKey, defaultValue = false)
    val (stopMusicOnTaskClear, onStopMusicOnTaskClearChange) = rememberPreference(StopMusicOnTaskClearKey, defaultValue = false)

    StepLayout(
        title = stringResource(R.string.player_and_audio),
        description = stringResource(R.string.player_and_audio_setup_desc)
    ) {
        // Audio Quality
        Text(stringResource(R.string.audio_quality), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Column(modifier = Modifier.padding(top = 8.dp)) {
            AudioQuality.values().forEach { quality ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAudioQualityChange(quality) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = audioQuality == quality,
                        onClick = { onAudioQualityChange(quality) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = when (quality) {
                        AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                        AudioQuality.MAX -> stringResource(R.string.audio_quality_max)
                        AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                        AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                    })
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Skip Silence
        WizardSettingsRow(
            title = stringResource(R.string.skip_silence),
            description = stringResource(R.string.skip_silence_setup_desc),
            icon = painterResource(R.drawable.fast_forward)
        ) {
            Switch(checked = skipSilence, onCheckedChange = onSkipSilenceChange)
        }

        // Stop music on task clear
        WizardSettingsRow(
            title = stringResource(R.string.stop_music_on_task_clear),
            description = stringResource(R.string.stop_music_setup_desc),
            icon = painterResource(R.drawable.clear_all)
        ) {
            Switch(checked = stopMusicOnTaskClear, onCheckedChange = onStopMusicOnTaskClearChange)
        }
    }
}

@Composable
fun AccountSettingsStep(navController: NavController) {
    val (innerTubeCookie, _) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }
    val (useLoginForBrowse, onUseLoginForBrowseChange) = rememberPreference(key = UseLoginForBrowse, defaultValue = false)

    StepLayout(
        title = stringResource(R.string.account),
        description = stringResource(R.string.login_description)
    ) {
        if (isLoggedIn) {
            Text(
                stringResource(R.string.login_successful),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        } else {
            Button(
                onClick = {
                    navController.navigate("login")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.login))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        WizardSettingsRow(
            title = stringResource(R.string.use_login_for_browse),
            description = stringResource(R.string.use_login_for_browse_desc),
            // Assuming R.drawable.person exists
            icon = painterResource(R.drawable.person),
            enabled = isLoggedIn
        ) {
            Switch(checked = useLoginForBrowse, onCheckedChange = onUseLoginForBrowseChange, enabled = isLoggedIn)
        }
    }
}


@Composable
fun FinishStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.setup_complete),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.setup_complete_description),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun WizardBottomBar(
    position: Int,
    maxPosition: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(80.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                enabled = position > 0
            ) {
                Icon(Icons.AutoMirrored.Rounded.NavigateBefore, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.back))
            }

            LinearProgressIndicator(
                progress = { (position.toFloat() / maxPosition) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            Button(
                onClick = {
                    if (position < maxPosition) {
                        onNext()
                    } else {
                        onFinish()
                    }
                }
            ) {
                Text(if (position < maxPosition) stringResource(R.string.next) else stringResource(R.string.finish))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    if (position < maxPosition) Icons.AutoMirrored.Rounded.NavigateNext else Icons.Rounded.CheckCircle,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun FeatureChip(
    icon: ImageVector? = null,
    painterIcon: Int? = null,
    text: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (painterIcon != null) {
                Icon(
                    painter = painterResource(painterIcon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun StepLayout(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun WizardSettingsRow(
    title: String,
    description: String,
    icon: Painter,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.alpha(if (enabled) 1f else 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    Text(text = title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = 0.7f)
                    )
                }
            }
            content()
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}
