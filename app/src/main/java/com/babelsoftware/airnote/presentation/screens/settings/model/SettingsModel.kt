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
import com.babelsoftware.airnote.data.repository.GeminiRepository
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
    private val geminiRepository: GeminiRepository
) : ViewModel() {
    var defaultRoute: String? = null

    fun loadDefaultRoute() {
        if (_settings.value.fingerprint == false && _settings.value.passcode == null && _settings.value.pattern == null) {
            defaultRoute == NavRoutes.Home.route
        } else {
            defaultRoute = _settings.value.defaultRouteType

        }
    }

    fun updateUserApiKey(newApiKey: String) {
        secureStorageRepository.saveUserApiKey(newApiKey)
        update(settings.value.copy(userGeminiApiKey = newApiKey))
    }

    fun loadUserApiKey() {
        val storedKey = secureStorageRepository.getUserApiKey() ?: ""
        update(settings.value.copy(userGeminiApiKey = storedKey))
    }

    private val _isVerifyingApiKey = mutableStateOf(false)
    val isVerifyingApiKey: State<Boolean> = _isVerifyingApiKey

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun verifyUserApiKey() {
        val keyToVerify = settings.value.userGeminiApiKey
        if (keyToVerify.isBlank()) {
            viewModelScope.launch { _uiEvent.send("Lütfen önce bir API anahtarı girin.") }
            return
        }

        viewModelScope.launch {
            _isVerifyingApiKey.value = true
            val result = geminiRepository.validateApiKey(keyToVerify)
            val message = if (result.isSuccess) {
                "API anahtarı geçerli ve kullanıma hazır! ✅"
            } else {
                "Geçersiz API anahtarı. Lütfen kontrol edin."
            }
            _uiEvent.send(message)
            _isVerifyingApiKey.value = false
        }
    }

    fun updateSelectedModel(modelName: String) {
        update(settings.value.copy(selectedModelName = modelName))
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
    }
}
