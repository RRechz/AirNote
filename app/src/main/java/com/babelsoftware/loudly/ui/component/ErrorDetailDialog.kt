package com.babelsoftware.loudly.ui.component

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babelsoftware.loudly.R

@Composable
fun ErrorDetailDialog(
    errorTitle: String,
    errorMessage: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = errorTitle, fontWeight = FontWeight.Bold) },
        text = {
            Box(modifier = Modifier.heightIn(max = 250.dp)) {
                LazyColumn {
                    item {
                        Text(text = errorMessage)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    shareError(context, errorMessage)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.send_to_developer))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

private fun shareError(context: Context, error: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Loudly Update Error:\n\n$error")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}