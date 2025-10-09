package com.babelsoftware.airnote.presentation.screens.settings.model

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.airnote.data.repository.BackupResult
import com.babelsoftware.airnote.data.repository.ImportExportRepository
import com.babelsoftware.airnote.domain.model.Settings
import com.babelsoftware.airnote.domain.usecase.ImportExportUseCase
import com.babelsoftware.airnote.domain.usecase.ImportResult
import com.babelsoftware.airnote.domain.usecase.NoteUseCase
import com.babelsoftware.airnote.domain.usecase.SettingsUseCase
import com.babelsoftware.airnote.presentation.components.GalleryObserver
import com.babelsoftware.airnote.presentation.navigation.NavRoutes
import com.babelsoftware.airnote.data.repository.SecureStorageRepository
import com.babelsoftware.airnote.BuildConfig
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.provider.StringProvider
import com.babelsoftware.airnote.data.repository.GeminiRepository
import com.babelsoftware.airnote.util.checkForUpdates
import com.babelsoftware.airnote.util.isNewerVersion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    val galleryObserver: GalleryObserver,
    val backup: ImportExportRepository,
    private val settingsUseCase: SettingsUseCase,
    val noteUseCase: NoteUseCase,
    private val importExportUseCase: ImportExportUseCase,
    private val secureStorageRepository: SecureStorageRepository,
    private val geminiRepository: GeminiRepository,
    private val stringProvider: StringProvider
) : ViewModel() {
    var defaultRoute: String? = null

    // ---> Update Check States
    private val _updateAvailable = mutableStateOf(false)
    val updateAvailable: State<Boolean> = _updateAvailable

    private val _latestVersion = mutableStateOf("")
    val latestVersion: State<String> = _latestVersion

    private val _showUpdateDialog = mutableStateOf(false)
    val showUpdateDialog: State<Boolean> = _showUpdateDialog

    private val _userApiKey = mutableStateOf("")
    val userApiKey: State<String> = _userApiKey

    private val _isVerifyingApiKey = mutableStateOf(false)
    val isVerifyingApiKey: State<Boolean> = _isVerifyingApiKey

    private val _isApiKeyVerified = mutableStateOf(false)
    val isApiKeyVerified: State<Boolean> = _isApiKeyVerified

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun checkForNewUpdate(context: Context) {
        viewModelScope.launch {
            val latestVersionFromGitHub = checkForUpdates()
            if (latestVersionFromGitHub != null) {
                val isNewer = isNewerVersion(latestVersionFromGitHub, version)
                _updateAvailable.value = isNewer
                if (isNewer) {
                    _latestVersion.value = latestVersionFromGitHub
                    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    val lastNotificationTime = sharedPreferences.getLong("last_update_notification_time", 0L)
                    val currentTime = System.currentTimeMillis()
                    // --->Show update pop-up every 6 hours
                    if (currentTime - lastNotificationTime > 6 * 60 * 60 * 1000) {
                        _showUpdateDialog.value = true
                        sharedPreferences.edit().putLong("last_update_notification_time", currentTime).apply()
                    }
                    // <---
                }
            }
        }
    }

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
    }
    // <---

    fun loadDefaultRoute() {
        if (_settings.value.fingerprint == false && _settings.value.passcode == null && _settings.value.pattern == null) {
            defaultRoute == NavRoutes.Home.route
        } else {
            defaultRoute = _settings.value.defaultRouteType

        }
    }

    fun updateUserApiKey(newApiKey: String) {
        _userApiKey.value = newApiKey
        if (_isApiKeyVerified.value) {
            _isApiKeyVerified.value = false
        }
    }

    private fun checkStoredApiKey() {
        val storedKey = secureStorageRepository.getUserApiKey() ?: ""
        _userApiKey.value = storedKey
        val storedModel = settings.value.selectedModelName

        if (storedKey.isNotBlank()) {
            viewModelScope.launch {
                val result = geminiRepository.validateApiKey(
                    apiKey = storedKey,
                    modelName = storedModel
                )
                _isApiKeyVerified.value = result.isSuccess
            }
        }
    }

    fun verifyUserApiKey() {
        val keyToVerify = _userApiKey.value
        val modelToVerify = settings.value.selectedModelName

        if (keyToVerify.isBlank()) {
            viewModelScope.launch { _uiEvent.send(stringProvider.getString(R.string.error_api_key_blank)) }
            return
        }

        viewModelScope.launch {
            _isVerifyingApiKey.value = true
            val result = geminiRepository.validateApiKey(
                apiKey = keyToVerify,
                modelName = modelToVerify
            )
            _isApiKeyVerified.value = result.isSuccess

            val message: String
            if (result.isSuccess) {
                secureStorageRepository.saveUserApiKey(keyToVerify)
                message = stringProvider.getString(R.string.api_key_validation_success)
            } else {
                message = stringProvider.getString(R.string.api_key_validation_failure)
            }
            _uiEvent.send(message)
            _isVerifyingApiKey.value = false
        }
    }

    fun updateSelectedModel(modelName: String) {
        update(settings.value.copy(selectedModelName = modelName))
        if (_isApiKeyVerified.value) {
            _isApiKeyVerified.value = false
        }
    }

    fun updateDefaultRoute(route: String) {
        _settings.value = _settings.value.copy(defaultRouteType = route)
        update(settings.value.copy(defaultRouteType = route))
    }

    val databaseUpdate = mutableStateOf(false)
    var password : String? = null

    private val _settings = mutableStateOf(Settings())
    var settings: State<Settings> = _settings

    private suspend fun loadSettings() {
        val loadedSettings = runBlocking(Dispatchers.IO) {
            settingsUseCase.loadSettingsFromRepository()
        }
        _settings.value = loadedSettings
        if (_settings.value.fingerprint == false && _settings.value.passcode == null && _settings.value.pattern == null) {

            defaultRoute = NavRoutes.Home.route
        } else {
            defaultRoute = loadedSettings.defaultRouteType
        }
    }

    fun update(newSettings: Settings) {
        _settings.value = newSettings.copy()
        viewModelScope.launch {
            settingsUseCase.saveSettingsToRepository(newSettings)
        }
    }


    fun onExportBackup(uri: Uri, context: Context) {
        viewModelScope.launch {
            val result = backup.exportBackup(uri, password)
            handleBackupResult(result, context)
            databaseUpdate.value = true
        }
    }

    fun onImportBackup(uri: Uri, context: Context) {
        viewModelScope.launch {
            val result = backup.importBackup(uri, password)
            handleBackupResult(result, context)
            databaseUpdate.value = true
        }
    }

    fun onImportFiles(uris: List<Uri>, context: Context) {
        viewModelScope.launch {
            importExportUseCase.importNotes(uris) { result ->
                handleImportResult(result, context)
            }
        }
    }

    // Taken from: https://stackoverflow.com/questions/74114067/get-list-of-locales-from-locale-config-in-android-13
    private fun getLocaleListFromXml(context: Context): LocaleListCompat {
        val tagsList = mutableListOf<CharSequence>()
        try {
            val xpp: XmlPullParser = context.resources.getXml(R.xml.locales_config)
            while (xpp.eventType != XmlPullParser.END_DOCUMENT) {
                if (xpp.eventType == XmlPullParser.START_TAG) {
                    if (xpp.name == "locale") {
                        tagsList.add(xpp.getAttributeValue(0))
                    }
                }
                xpp.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return LocaleListCompat.forLanguageTags(tagsList.joinToString(","))
    }

    fun getSupportedLanguages(context: Context): Map<String, String> {
        val localeList = getLocaleListFromXml(context)
        val map = mutableMapOf<String, String>()

        for (a in 0 until localeList.size()) {
            localeList[a].let {
                it?.let { it1 -> map.put(it1.getDisplayName(it), it.toLanguageTag()) }
            }
        }
        return map
    }

    private fun handleBackupResult(result: BackupResult, context: Context) {
        when (result) {
            is BackupResult.Success -> {}
            is BackupResult.Error -> showToast("Error", context)
            BackupResult.BadPassword -> showToast(context.getString(R.string.detabase_restore_error), context)
        }
    }

    private fun handleImportResult(result: ImportResult, context: Context) {
        when (result.successful) {
            result.total -> {showToast(context.getString(R.string.file_import_success), context)}
            0 -> {showToast(context.getString(R.string.file_import_error), context)}
            else -> {showToast(context.getString(R.string.file_import_partial_error), context)}
        }
    }

    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    val version: String = BuildConfig.VERSION_NAME
    val build: String = BuildConfig.BUILD_TYPE

    init {
        runBlocking {
            loadSettings()
        }
        checkStoredApiKey()
    }
}
