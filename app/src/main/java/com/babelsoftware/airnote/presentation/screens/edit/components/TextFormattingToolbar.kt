package com.babelsoftware.airnote.presentation.screens.edit.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.FormatUnderlined
import androidx.compose.material.icons.rounded.Highlight
import androidx.compose.material.icons.rounded.HorizontalRule
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.StrikethroughS
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.babelsoftware.airnote.presentation.components.getExternalStorageDir
import com.babelsoftware.airnote.presentation.components.getImageName
import com.babelsoftware.airnote.presentation.screens.edit.model.EditViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class ToolbarItem(
    val icon: ImageVector,
    val contentDescription: String,
    val color: Color,
    val onClickAction: () -> Unit,
)

@Composable
fun TextFormattingToolbar(viewModel: EditViewModel) {
    val colorIcon = MaterialTheme.colorScheme.onSurfaceVariant
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val savedUri = saveImageToAppStorage(context, it)
            viewModel.insertText("!($savedUri)")
        }
    }

    val toolbarItems = remember {
        listOf(
            ToolbarItem(Icons.Rounded.FormatBold, "Bold", color = colorIcon) {
                viewModel.insertText("****", offset = -2, newLine = false)
            },
            ToolbarItem(Icons.Rounded.FormatItalic, "Italic", color = colorIcon) {
                viewModel.insertText("**", offset = -1, newLine = false)
            },
            ToolbarItem(Icons.Rounded.FormatUnderlined, "Underline", color = colorIcon) {
                viewModel.insertText("__", -1 , newLine = false)
            },
            ToolbarItem(Icons.Rounded.StrikethroughS, "Strikethrough", color = colorIcon) {
                viewModel.insertText("~~~~", -2 , newLine = false)
            },
            ToolbarItem(Icons.Rounded.Highlight, "Highlight", color = colorIcon) {
                viewModel.insertText("====", offset = -2, newLine = false)
            },
            ToolbarItem(Icons.Rounded.Title, "Header", color = colorIcon) {
                viewModel.insertText("# ")
            },
            ToolbarItem(Icons.Rounded.FormatQuote, "Quote", color = colorIcon) {
                viewModel.insertText("> ", newLine = true)
            },
            ToolbarItem(Icons.Rounded.HorizontalRule, "Horizontal Rule", color = colorIcon) {
                viewModel.insertText("\n---\n", newLine = true)
            },
            ToolbarItem(Icons.AutoMirrored.Rounded.FormatListBulleted, "Bullet List", color = colorIcon) {
                viewModel.insertText("- ")
            },
            ToolbarItem(Icons.Rounded.FormatListNumbered, "Numbered List", color = colorIcon) {
                viewModel.insertText("1. ")
            },
            ToolbarItem(Icons.Rounded.CheckBox, "Checkbox", color = colorIcon) {
                viewModel.insertText("[ ] ")
            },
            ToolbarItem(Icons.Rounded.Link, "Insert Link", color = colorIcon) {
                viewModel.insertText("[](url)", offset = -6, newLine = false)
            },
            ToolbarItem(Icons.Rounded.Image, "Insert Image", color = colorIcon) {
                launcher.launch("image/*")
            },
            ToolbarItem(Icons.Rounded.TableChart, "Insert Table", color = colorIcon) {
                viewModel.insertText("| Header | Header |\n| --- | --- |\n| Cell | Cell |", newLine = true)
            },
            ToolbarItem(Icons.Rounded.Code, "Code Block", color = colorIcon) {
                viewModel.insertText("```\n\n```", -4)
            },
            ToolbarItem(Icons.Rounded.DataObject, "Inline Code", color = colorIcon) {
                viewModel.insertText("``", -1, newLine = false)
            },
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.toggleMinimalAiUi(true) },
                enabled = true
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "Edit with AI",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(toolbarItems) { item ->
                    IconButton(
                        onClick = { item.onClickAction.invoke() },
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.contentDescription,
                            modifier = Modifier.size(24.dp),
                            tint = item.color,
                        )
                    }
                }
            }
        }
    }
}

private fun saveImageToAppStorage(context: Context, uri: Uri): String {
    val appStorageDir = getExternalStorageDir(context)
    if (!appStorageDir.exists()) {
        appStorageDir.mkdirs()
    }
    val imageFile = File(appStorageDir, getImageName(uri))

    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    inputStream?.use { input ->
        FileOutputStream(imageFile).use { output ->
            input.copyTo(output)
        }
    }

    inputStream?.close()
    return imageFile.path.toString()
}