package com.quare.bibleplanner.android.feature.profile

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileColorScheme
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import kotlinx.datetime.toJavaLocalDate
import software.quare.loadable.valueOrNull
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val PERCENT = 100

@Composable
internal fun themeSubtitle(state: ProfileUiState): String? = state.theme.valueOrNull()?.let { theme ->
    val themeName = stringResource(theme.toStringRes())
    state.colorScheme.valueOrNull()?.let { colorScheme ->
        "$themeName • ${stringResource(colorScheme.toStringRes())}"
    } ?: themeName
}

@Composable
internal fun languageSubtitle(state: ProfileUiState): String? = state.selectedLanguage.valueOrNull()?.let { language ->
    stringResource(language.toStringRes())
}

internal fun toBibleVersionSubtitle(state: ProfileUiState): String? =
    state.bibleVersionName.valueOrNull()?.let { name ->
        val downloadProgress = state.bibleDownloadProgress.valueOrNull()
        if (downloadProgress != null) "$name (${(downloadProgress * PERCENT).toInt()}%)" else name
    }

@Composable
internal fun planStartDateSubtitle(state: ProfileUiState): String? {
    val planStartDate = state.planStartDate.valueOrNull() ?: return null
    // Why: the app language can differ from the system's, and the configuration carries the one the app shows.
    val locale = LocalConfiguration.current.locales[0]
    val dateLabel = remember(planStartDate, locale) {
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
            .format(planStartDate.toJavaLocalDate())
    }
    return if (planStartDate == state.currentDate) {
        "${stringResource(R.string.profile_today)}, $dateLabel"
    } else {
        dateLabel
    }
}

@Composable
internal fun studySuggestionSubtitle(state: ProfileUiState): String? =
    state.studySuggestion.valueOrNull()?.let { settings -> stringResource(settings.toSubtitleRes()) }

@StringRes
private fun StudySuggestionSettingsModel.toSubtitleRes(): Int = when {
    !isEnabled -> R.string.profile_study_suggestion_disabled
    mode == StudySuggestionMode.BANNER -> R.string.profile_study_suggestion_enabled_banner
    else -> R.string.profile_study_suggestion_enabled_dialog
}

@StringRes
private fun Theme.toStringRes(): Int = when (this) {
    Theme.LIGHT -> R.string.profile_theme_light
    Theme.DARK -> R.string.profile_theme_dark
    Theme.SYSTEM -> R.string.profile_theme_system
}

@StringRes
private fun ProfileColorScheme.toStringRes(): Int = when (this) {
    ProfileColorScheme.DYNAMIC_COLORS -> R.string.profile_dynamic_colors
    ProfileColorScheme.STANDARD_CONTRAST -> R.string.profile_contrast_standard
    ProfileColorScheme.MEDIUM_CONTRAST -> R.string.profile_contrast_medium
    ProfileColorScheme.HIGH_CONTRAST -> R.string.profile_contrast_high
}

@StringRes
private fun Language.toStringRes(): Int = when (this) {
    Language.ENGLISH -> R.string.profile_language_english
    Language.PORTUGUESE_BRAZIL -> R.string.profile_language_portuguese_brazil
    Language.SPANISH -> R.string.profile_language_spanish
}
