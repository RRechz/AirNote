/*
 * Copyright (c) 2025 Babel Software.
 */

package com.babelsoftware.airnote.presentation.screens.settings.model

import androidx.compose.ui.graphics.vector.ImageVector

sealed class IconResource {
    data class Vector(val imageVector: ImageVector) : IconResource()
    data class Url(val url: String) : IconResource()
}