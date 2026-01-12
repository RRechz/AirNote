package com.babelsoftware.loudly.models

import com.babelsoftware.innertube.models.YTItem

data class ItemsPage(
    val items: List<YTItem>,
    val continuation: String?,
)
