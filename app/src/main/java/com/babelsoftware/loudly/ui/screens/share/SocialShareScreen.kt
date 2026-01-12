package com.babelsoftware.loudly.ui.screens.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.models.MediaMetadata
import com.babelsoftware.loudly.ui.component.ChipStyleButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CaptureController {
    var capture: (() -> Unit)? = null
}

@Composable
fun rememberCaptureController(): CaptureController {
    return remember { CaptureController() }
}

@Composable
fun Capturable(
    controller: CaptureController,
    onBitmapCaptured: (Bitmap) -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var viewToCapture: View? by remember { mutableStateOf(null) }

    controller.capture = {
        viewToCapture?.let { view ->
            coroutineScope.launch {
                view.isDrawingCacheEnabled = true
                view.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
                val cachedBitmap = view.drawingCache
                val bitmap = Bitmap.createBitmap(cachedBitmap)
                view.isDrawingCacheEnabled = false

                onBitmapCaptured(bitmap)
            }
        }
    }

    AndroidView(
        factory = {
            ComposeView(it).apply {
                setContent {
                    content()
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = {
            viewToCapture = it
        }
    )
}


@Composable
fun SocialShareScreen(
    mediaMetadata: MediaMetadata,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }

    val captureController = rememberCaptureController()

    val defaultDominantColor = MaterialTheme.colorScheme.surfaceVariant
    val defaultVibrantColor = MaterialTheme.colorScheme.primary

    var dominantColor by remember { mutableStateOf(defaultDominantColor) }
    var vibrantColor by remember { mutableStateOf(defaultVibrantColor) }

    LaunchedEffect(mediaMetadata.thumbnailUrl) {
        withContext(Dispatchers.IO) {
            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(mediaMetadata.thumbnailUrl)
                .allowHardware(false)
                .build()
            val bitmap = imageLoader.execute(request).drawable?.toBitmap()
            if (bitmap != null) {
                Palette.from(bitmap).generate { palette ->
                    palette?.let {
                        dominantColor = Color(it.getDominantColor(defaultDominantColor.toArgb()))
                        vibrantColor = Color(it.getVibrantColor(defaultVibrantColor.toArgb()))
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.create_your_story), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Kapat")
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(9f / 16f)
                        ){
                            Capturable(
                                controller = captureController,
                                onBitmapCaptured = { bitmap ->
                                    shareBitmap(context, bitmap)
                                    isGenerating = false
                                    onDismiss()
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(vibrantColor.copy(alpha = 0.8f), dominantColor.copy(alpha = 0.6f))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SharePreviewContent(mediaMetadata)
                                }
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator()
                        } else {
                            ChipStyleButton(
                                onClick = {
                                    isGenerating = true
                                    captureController.capture?.invoke()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.share_on_story))
                            }
                            TextButton(onClick = {
                                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val songUrl = "https://music.youtube.com/watch?v=${mediaMetadata.id}"
                                val clipData = ClipData.newPlainText("Loudly Şarkı Linki", songUrl)
                                clipboardManager.setPrimaryClip(clipData)
                                Toast.makeText(context, "Link copied to the clipboard!", Toast.LENGTH_SHORT).show()
                            }) {
                                Text(stringResource(R.string.copy_song_link))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SharePreviewContent(mediaMetadata: MediaMetadata) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            AsyncImage(
                model = mediaMetadata.thumbnailUrl,
                contentDescription = "Albüm Kapağı",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp), clip = true)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = mediaMetadata.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp),
                style = LocalTextStyle.current.copy(shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), offset = Offset(2f, 2f), blurRadius = 4f))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mediaMetadata.artists.joinToString { it.name },
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp),
                style = LocalTextStyle.current.copy(shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), offset = Offset(1f, 1f), blurRadius = 2f))
            )
            Spacer(modifier = Modifier.weight(0.2f))

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = 0.3f,
                    onValueChange = {},
                    enabled = false,
                    colors = SliderDefaults.colors(
                        disabledThumbColor = Color.White,
                        disabledActiveTrackColor = Color.White,
                        disabledInactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(R.drawable.skip_previous), "Önceki", tint = Color.White, modifier = Modifier.size(36.dp))
                    Icon(painterResource(R.drawable.pause), "Durdur", tint = Color.Black, modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(8.dp))
                    Icon(painterResource(R.drawable.skip_next), "Sonraki", tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }
            Spacer(modifier = Modifier.weight(0.3f))
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(R.drawable.loudly_monochrome),
                contentDescription = "Loudly Logo", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(R.string.streamed_on_loudly),
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                style = LocalTextStyle.current.copy(shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), offset = Offset(1f, 1f), blurRadius = 2f))
            )
        }
    }
}

private fun drawableToBitmap(context: Context, @DrawableRes drawableId: Int, size: Int): Bitmap {
    val drawable: Drawable = context.getDrawable(drawableId)!!
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val tintColor = if (drawableId == R.drawable.pause) android.graphics.Color.BLACK else android.graphics.Color.WHITE
    DrawableCompat.setTint(drawable, tintColor)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

private fun drawableToBitmap(context: Context, @DrawableRes drawableId: Int): Bitmap {
    val drawable: Drawable = context.getDrawable(drawableId)!!
    DrawableCompat.setTint(drawable, android.graphics.Color.WHITE)
    if (drawableId == R.drawable.pause) {
        DrawableCompat.setTint(drawable, android.graphics.Color.BLACK)
    }
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

private fun shareBitmap(context: Context, bitmap: Bitmap) {
    val cachePath = File(context.cacheDir, "images")
    cachePath.mkdirs()
    val file = File(cachePath, "social_share.png")
    val fileOutputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
    fileOutputStream.close()

    val authority = "${context.packageName}.FileProvider"
    val contentUri = FileProvider.getUriForFile(context, authority, file)
    val clipData = ClipData.newUri(context.contentResolver, "Loudly Share", contentUri)

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/png"
        this.clipData = clipData
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Hikayede Paylaş"))
}