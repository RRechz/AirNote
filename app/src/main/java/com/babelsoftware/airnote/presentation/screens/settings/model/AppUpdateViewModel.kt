package com.babelsoftware.airnote.presentation.screens.settings.model

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.airnote.data.provider.StringProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject

// A sealed class representing different states of the update process
sealed class UpdateState {
    object Idle : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data class ReadyToInstall(val apkUri: Uri) : UpdateState()
    data class Failed(val error: String) : UpdateState()
}

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val stringProvider: StringProvider,
    private val app: Application
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState = _updateState.asStateFlow()
    private var downloadJob: Job? = null

    fun downloadAndInstallApk(downloadUrl: String) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _updateState.value = UpdateState.Downloading(0)
                val url = URL(downloadUrl)
                val connection = url.openConnection()
                connection.connect()
                val fileSize = connection.contentLength
                val inputStream = connection.getInputStream()
                val file = File(app.cacheDir, "update.apk")
                val outputStream = FileOutputStream(file)
                var total: Long = 0
                val data = ByteArray(1024 * 4)
                var count: Int
                while (inputStream.read(data).also { count = it } != -1) {
                    if (!isActive) {
                        outputStream.close()
                        inputStream.close()
                        file.delete()
                        throw CancellationException("Download cancelled by user.")
                    }

                    total += count.toLong()
                    outputStream.write(data, 0, count)
                    if (fileSize > 0) {
                        val progress = (total * 100 / fileSize).toInt()
                        _updateState.value = UpdateState.Downloading(progress)
                    }
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()

                val authority = "${app.packageName}.provider"
                val apkUri = FileProvider.getUriForFile(app, authority, file)
                _updateState.value = UpdateState.ReadyToInstall(apkUri)

            } catch (e: CancellationException) {
                _updateState.value = UpdateState.Idle
                println("İndirme kullanıcı tarafından iptal edildi.")
            } catch (e: Exception) {
                e.printStackTrace()
                _updateState.value = UpdateState.Failed("Güncelleme indirilirken bir hata oluştu: ${e.message}")
            }
        }
    }
    fun resetState() {
        downloadJob?.cancel()
        downloadJob = null
        _updateState.value = UpdateState.Idle
    }
}