package com.babelsoftware.loudly.ui.screens.guides

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.babelsoftware.loudly.R

/**
 * Basic structure for property introduction steps.
 */
@Composable
fun FeatureGuideStep(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun StoryShareFeatureStep(modifier: Modifier = Modifier) {
    FeatureGuideStep(
        title = stringResource(R.string.create_your_story),
        description = stringResource(R.string.share_your_story_description),
        modifier = modifier
    ) {
        StorySharePreview()
    }
}

@Composable
fun SettingsHeaderFeatureStep(modifier: Modifier = Modifier) {
    FeatureGuideStep(
        title = stringResource(R.string.customizable_settings),
        description = stringResource(R.string.customizable_settings_description),
        modifier = modifier
    ) {
        SettingsHeaderPreview()
    }
}

@Composable
fun PlayerPlaylistFeatureStep(modifier: Modifier = Modifier) {
    FeatureGuideStep(
        title = stringResource(R.string.new_song_list),
        description = stringResource(R.string.new_song_list_description),
        modifier = modifier
    ) {
        PlayerPlaylistPreview()
    }
}

@Composable
fun NewSettingsUiFeatureStep(modifier: Modifier = Modifier) {
    FeatureGuideStep(
        title = stringResource(R.string.remastered_settings),
        description = stringResource(R.string.remastered_settings_description),
        modifier = modifier
    ) {
        NewSettingsUiPreview()
    }
}

@Composable
fun ClassicWidgetFeatureStep(modifier: Modifier = Modifier) {
    FeatureGuideStep(
        title = stringResource(R.string.classic_music_widget),
        description = stringResource(R.string.classic_music_widget_description),
        modifier = modifier
    ) {
        ClassicWidgetPreview()
    }
}

// Special preview designs
@Composable
private fun StorySharePreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.8f)))
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth(0.5f).height(6.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.weight(0.2f))
            Column(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)))
                    Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(Color.White))
                    Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)))
                }
            }
            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}

@Composable
private fun SettingsHeaderPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(15.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
                Spacer(modifier = Modifier.height(2.dp))
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(20.dp).clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)).background(MaterialTheme.colorScheme.primary))
            }
        }
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(6) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(18.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.width(80.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
                        Box(modifier = Modifier.width(50.dp).height(6.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                        Box(modifier = Modifier.width(80.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
                        Box(modifier = Modifier.width(50.dp).height(6.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                        Box(modifier = Modifier.width(80.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
                        Box(modifier = Modifier.width(50.dp).height(6.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerPlaylistPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    MaterialTheme.colorScheme.surfaceVariant
                )
            )
        ))
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary))
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp)
                .fillMaxWidth(0.5f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
    }
}

@Composable
private fun NewSettingsUiPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.width(50.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
        Box(modifier = Modifier.fillMaxWidth().height(30.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.width(60.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.width(60.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
        Box(modifier = Modifier.width(50.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.width(60.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
        Box(modifier = Modifier.fillMaxWidth().height(30.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
            Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
        }
    }
}

@Composable
private fun ClassicWidgetPreview() {
    Image(
        painter = painterResource(id = R.drawable.widget_preview_classic),
        contentDescription = "Classic Widget Preview",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}