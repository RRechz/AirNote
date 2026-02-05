package com.babelsoftware.loudly.ui.screens.settings.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.loudly.BuildConfig
import com.babelsoftware.loudly.utils.getLatestReleaseInfo
import com.babelsoftware.loudly.utils.isNewerVersion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    val updateAvailable = mutableStateOf(false)

    init {
        checkForUpdates()
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            val releaseInfo = getLatestReleaseInfo()
            if (releaseInfo != null) {
                updateAvailable.value = isNewerVersion(releaseInfo.tagName, BuildConfig.VERSION_NAME)
            }
        }
    }
}