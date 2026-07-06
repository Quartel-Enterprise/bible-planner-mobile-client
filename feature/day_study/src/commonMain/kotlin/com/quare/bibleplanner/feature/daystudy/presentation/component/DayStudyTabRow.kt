package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_tab_context
import bibleplanner.feature.day_study.generated.resources.ai_tab_questions
import bibleplanner.feature.day_study.generated.resources.ai_tab_summary
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyTab
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DayStudyTabRow(
    selectedTab: DayStudyTab,
    onSelectTab: (DayStudyTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    SecondaryTabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = modifier,
    ) {
        DayStudyTab.entries.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onSelectTab(tab) },
                text = { Text(stringResource(tab.titleRes)) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val DayStudyTab.titleRes: StringResource
    get() = when (this) {
        DayStudyTab.SUMMARY -> Res.string.ai_tab_summary
        DayStudyTab.CONTEXT -> Res.string.ai_tab_context
        DayStudyTab.QUESTIONS -> Res.string.ai_tab_questions
    }
