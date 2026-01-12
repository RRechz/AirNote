package com.babelsoftware.loudly.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.media3.common.PlaybackException
import com.babelsoftware.loudly.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogReportHelper {

    fun createAndShareErrorReport(
        context: Context,
        error: PlaybackException,
        mediaId: String?
    ) {
        val logContent = buildString {
            appendLine("Loudly Error Report")
            appendLine("Time Stamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            appendLine("App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("--------------------")
            appendLine("Error Details:")
            appendLine("   Song ID: ${mediaId ?: "Unknow"}")
            appendLine("   Error Code: ${error.errorCodeName} (${error.errorCode})")
            appendLine("   Error Message: ${error.message}")
            appendLine("\nException Chain (Cause):")
            var cause: Throwable? = error.cause
            while (cause != null) {
                appendLine("   - ${cause.javaClass.simpleName}: ${cause.message}")
                cause = cause.cause
            }
            appendLine("--------------------")
        }

        try {
            // 1. Log dosyasını oluştur
            val logFile = File(context.cacheDir, "loudly_error_report_${System.currentTimeMillis()}.txt")
            logFile.writeText(logContent)

            // 2. FileProvider ile URI al
            val fileUri = FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.provider", // Manifest'te tanımlanmalı
                logFile
            )

            // 3. Paylaşım Intent'ini başlat
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Loudly Eror Report")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share Error Report...")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

        } catch (e: Exception) {
            // Dosya oluşturma veya paylaşma sırasında hata olursa...
            e.printStackTrace()
        }
    }
}