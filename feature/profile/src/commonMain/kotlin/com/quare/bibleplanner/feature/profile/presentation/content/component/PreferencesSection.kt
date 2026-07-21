package com.quare.bibleplanner.feature.profile.presentation.content.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.preferences.app_language.generated.resources.language_english
import bibleplanner.feature.preferences.app_language.generated.resources.language_portuguese_brazil
import bibleplanner.feature.preferences.app_language.generated.resources.language_spanish
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.preferences
import bibleplanner.feature.profile.generated.resources.today
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.profile.presentation.factory.ProfileMenuOptionsFactory
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import bibleplanner.feature.preferences.app_language.generated.resources.Res as AppLanguageRes

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun PreferencesSection(
    modifier: Modifier = Modifier,
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeaderText(title = stringResource(Res.string.preferences))
        SectionCard {
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.theme,
                subtitle = themeSubtitle(state),
                isSubtitleLoading = state.themeRes is Loadable.Loading,
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.THEME)) },
            )
            HorizontalDivider()
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.appLanguage,
                subtitle = state.selectedLanguage.valueOrNull()?.let { stringResource(it.toStringResource()) },
                isSubtitleLoading = state.selectedLanguage is Loadable.Loading,
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.APP_LANGUAGE)) },
            )
            HorizontalDivider()
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.bibleVersion,
                subtitle = bibleVersionSubtitle(state),
                isSubtitleLoading = state.bibleVersionName is Loadable.Loading,
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.BIBLE_VERSION)) },
            )
            HorizontalDivider()
            ProfileMenuItem(
                itemModel = ProfileMenuOptionsFactory.editStartDate,
                subtitle = state.planStartDate.valueOrNull()?.let { startDate ->
                    formatPlanStartDateSubtitle(startDate, state.currentDate)
                },
                isSubtitleLoading = state.planStartDate is Loadable.Loading,
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.EDIT_PLAN_START_DAY)) },
            )
        }
    }
}

@Composable
private fun themeSubtitle(state: ProfileUiState): String? = state.themeRes.valueOrNull()?.let { themeRes ->
    val contrastRes = state.contrastRes.valueOrNull()
    if (contrastRes != null) {
        "${stringResource(themeRes)} • ${stringResource(contrastRes)}"
    } else {
        stringResource(themeRes)
    }
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

internal fun Language.toStringResource(): StringResource = when (this) {
    Language.ENGLISH -> AppLanguageRes.string.language_english
    Language.PORTUGUESE_BRAZIL -> AppLanguageRes.string.language_portuguese_brazil
    Language.SPANISH -> AppLanguageRes.string.language_spanish
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
