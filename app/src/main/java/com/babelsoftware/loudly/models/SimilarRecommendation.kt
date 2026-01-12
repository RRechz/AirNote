package com.babelsoftware.loudly.models

import com.babelsoftware.innertube.models.YTItem
import com.babelsoftware.loudly.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
