package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_offline_message
import bibleplanner.feature.day_study.generated.resources.ai_study_wait_for_generations
import com.quare.bibleplanner.core.books.util.toReadingLabel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import software.quare.loadable.Loadable

@Composable
fun DayStudySection(
    passages: List<PassageModel>,
    dayRoute: DayNavRoute,
    onOpenPaywall: () -> Unit,
    onOpenLoginWarning: () -> Unit,
    onShowSnackBar: (String) -> Unit,
    onNavigateToStudy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<DayStudyViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val label = passages.toReadingLabel()

    LaunchedEffect(passages, dayRoute, label) {
        viewModel.onEvent(DayStudyUiEvent.OnStart(passages, dayRoute, label))
    }

    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            DayStudyUiAction.NavigateToStudy -> onNavigateToStudy()

            DayStudyUiAction.NavigateToPaywall -> onOpenPaywall()

            DayStudyUiAction.NavigateToLoginWarning -> onOpenLoginWarning()

            DayStudyUiAction.ShowOffline -> onShowSnackBar(getString(Res.string.ai_study_offline_message))

            is DayStudyUiAction.ShowWaitForGenerations -> onShowSnackBar(
                getPluralString(Res.plurals.ai_study_wait_for_generations, action.count, action.count),
            )
        }
    }

    when (val card = uiState.card) {
        is Loadable.Loading -> AiStudyEntryCardSkeleton(modifier = modifier)

        is Loadable.Loaded -> AiStudyEntryCard(
            card = card.value,
            generation = uiState.generation,
            isOpening = uiState.isOpeningStudy,
            onClick = { viewModel.onEvent(DayStudyUiEvent.OnCardClick) },
            modifier = modifier,
        )
    }
}
