package com.babelsoftware.airnote.data.provider

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StringProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Basit metinler için
    fun getString(stringResId: Int): String {
        return context.getString(stringResId)
    }

    // Formatlanmış metinler için
    fun getString(stringResId: Int, vararg formatArgs: Any): String {
        return context.getString(stringResId, *formatArgs)
    }
}