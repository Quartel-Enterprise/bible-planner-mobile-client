package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.preferences
import bibleplanner.feature.more.generated.resources.today
import com.quare.bibleplanner.feature.more.presentation.factory.MoreMenuOptionsFactory
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.ui.utils.toStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun PreferencesSection(
    modifier: Modifier = Modifier,
    state: MoreUiState.Loaded,
    onEvent: (MoreUiEvent) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeaderText(title = stringResource(Res.string.preferences))
        SectionCard {
            val themeSubtitle = if (state.contrastRes != null) {
                "${stringResource(state.themeRes)} • ${stringResource(state.contrastRes)}"
            } else {
                stringResource(state.themeRes)
            }
            MoreMenuItem(
                itemModel = MoreMenuOptionsFactory.theme,
                subtitle = themeSubtitle,
                onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.THEME)) },
            )
            HorizontalDivider()
            MoreMenuItem(
                itemModel = MoreMenuOptionsFactory.bibleVersion,
                subtitle = state.bibleVersionName?.let { safeBibleVersionName ->
                    if (state.bibleDownloadProgress != null) {
                        "$safeBibleVersionName (${(state.bibleDownloadProgress * 100).toInt()}%)"
                    } else {
                        safeBibleVersionName
                    }
                },
                onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.BIBLE_VERSION)) },
            )
            HorizontalDivider()
            MoreMenuItem(
                itemModel = MoreMenuOptionsFactory.editStartDate,
                subtitle = formatPlanStartDateSubtitle(state.planStartDate, state.currentDate),
                onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.EDIT_PLAN_START_DAY)) },
            )
        }
    }
}

@Composable
private fun formatPlanStartDateSubtitle(
    planStartDate: kotlinx.datetime.LocalDate?,
    currentDate: kotlinx.datetime.LocalDate,
): String = planStartDate
    ?.let { date ->
        val isToday = date == currentDate
        val prefix = if (isToday) "${stringResource(Res.string.today)}, " else ""
        val month = stringResource(date.month.toStringResource()).take(3)
        "$prefix${date.day} $month ${date.year}"
    }.orEmpty()
