package com.quare.bibleplanner.feature.profile.presentation.content.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.contrast_high
import bibleplanner.feature.profile.generated.resources.contrast_medium
import bibleplanner.feature.profile.generated.resources.contrast_standard
import bibleplanner.feature.profile.generated.resources.dynamic_colors
import bibleplanner.feature.profile.generated.resources.preferences
import bibleplanner.feature.profile.generated.resources.study_suggestion_disabled
import bibleplanner.feature.profile.generated.resources.study_suggestion_enabled_banner
import bibleplanner.feature.profile.generated.resources.study_suggestion_enabled_dialog
import bibleplanner.feature.profile.generated.resources.theme_dark
import bibleplanner.feature.profile.generated.resources.theme_light
import bibleplanner.feature.profile.generated.resources.theme_system
import bibleplanner.feature.profile.generated.resources.today
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.feature.profile.presentation.factory.ProfileMenuOptionsFactory
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileColorScheme
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import com.quare.bibleplanner.ui.component.language.toStringResource
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import software.quare.loadable.isLoading
import software.quare.loadable.valueOrNull

@Composable
internal fun PreferencesSection(
    modifier: Modifier = Modifier,
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionHeaderText(title = stringResource(Res.string.preferences))
        SectionCard {
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.theme,
                subtitle = themeSubtitle(state),
                isSubtitleLoading = state.theme.isLoading(),
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.THEME)) },
            )
            HorizontalDivider()
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.appLanguage,
                subtitle = state.selectedLanguage.valueOrNull()?.let { stringResource(it.toStringResource()) },
                isSubtitleLoading = state.selectedLanguage.isLoading(),
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.APP_LANGUAGE)) },
            )
            HorizontalDivider()
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.bibleVersion,
                subtitle = bibleVersionSubtitle(state),
                isSubtitleLoading = state.bibleVersionName.isLoading(),
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.BIBLE_VERSION)) },
            )
            HorizontalDivider()
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.editStartDate,
                subtitle = state.planStartDate.valueOrNull()?.let { startDate ->
                    formatPlanStartDateSubtitle(startDate, state.currentDate)
                },
                isSubtitleLoading = state.planStartDate.isLoading(),
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.EDIT_PLAN_START_DAY)) },
            )
            HorizontalDivider()
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.studySuggestion,
                subtitle = state.studySuggestion.valueOrNull()?.let { settings ->
                    stringResource(settings.toSubtitleResource())
                },
                isSubtitleLoading = state.studySuggestion.isLoading(),
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.STUDY_SUGGESTION)) },
            )
        }
    }
}

private fun StudySuggestionSettingsModel.toSubtitleResource(): StringResource = when {
    !isEnabled -> Res.string.study_suggestion_disabled
    mode == StudySuggestionMode.BANNER -> Res.string.study_suggestion_enabled_banner
    else -> Res.string.study_suggestion_enabled_dialog
}

@Composable
private fun themeSubtitle(state: ProfileUiState): String? = state.theme.valueOrNull()?.let { theme ->
    val themeName = stringResource(theme.toStringResource())
    state.colorScheme.valueOrNull()?.let { colorScheme ->
        "$themeName • ${stringResource(colorScheme.toStringResource())}"
    } ?: themeName
}

private fun Theme.toStringResource(): StringResource = when (this) {
    Theme.LIGHT -> Res.string.theme_light
    Theme.DARK -> Res.string.theme_dark
    Theme.SYSTEM -> Res.string.theme_system
}

private fun ProfileColorScheme.toStringResource(): StringResource = when (this) {
    ProfileColorScheme.DYNAMIC_COLORS -> Res.string.dynamic_colors
    ProfileColorScheme.STANDARD_CONTRAST -> Res.string.contrast_standard
    ProfileColorScheme.MEDIUM_CONTRAST -> Res.string.contrast_medium
    ProfileColorScheme.HIGH_CONTRAST -> Res.string.contrast_high
}

@Composable
private fun bibleVersionSubtitle(state: ProfileUiState): String? = state.bibleVersionName.valueOrNull()?.let { name ->
    val downloadProgress = state.bibleDownloadProgress.valueOrNull()
    if (downloadProgress != null) {
        "$name (${(downloadProgress * 100).toInt()}%)"
    } else {
        name
    }
}

@Composable
private fun formatPlanStartDateSubtitle(
    planStartDate: LocalDate,
    currentDate: LocalDate,
): String {
    val isToday = planStartDate == currentDate
    val prefix = if (isToday) "${stringResource(Res.string.today)}, " else ""
    val month = stringResource(planStartDate.month.toStringResource()).take(3)
    return "$prefix${planStartDate.day} $month ${planStartDate.year}"
}
