package com.quare.bibleplanner.feature.applanguage.presentation.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import com.quare.bibleplanner.core.utils.locale.Language

@Composable
actual fun rememberApplyLanguage(): (Language) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { language -> context.applyAppLanguageLocale(language) }
    }
}

/**
 * Persists [language] as the app locale (read by `MainActivity.attachBaseContext`) and recreates the
 * current activity to apply it. Guarded against the stored value so re-applying the active locale —
 * on startup or as an echo of a sync change already applied — does not trigger a recreate loop.
 */
internal fun Context.applyAppLanguageLocale(language: Language) {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val currentTag = prefs.getString(KEY_APP_LANGUAGE, null)
    val newTag = language.toLocaleTag()
    if (currentTag == newTag) return
    prefs.edit { putString(KEY_APP_LANGUAGE, newTag) }
    if (currentTag != null) {
        findActivity()?.recreate()
    }
}

private fun Context.findActivity(): ComponentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is ComponentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun Language.toLocaleTag(): String = when (this) {
    Language.ENGLISH -> "en"
    Language.PORTUGUESE_BRAZIL -> "pt-BR"
    Language.SPANISH -> "es"
}

internal const val PREFS_NAME = "app_prefs"
internal const val KEY_APP_LANGUAGE = "app_language"
