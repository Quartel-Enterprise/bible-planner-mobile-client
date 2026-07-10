package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.books.util.toReadingLabel
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DayStudySection(
    passages: List<PassageModel>,
    dayRoute: DayNavRoute,
    onOpenPaywall: () -> Unit,
    onOpenLoginWarning: () -> Unit,
    onShowSnackBar: (String) -> Unit,
    modifier: Modifier = Modifier,
    inline: Boolean = false,
) {
    val viewModel = koinViewModel<DayStudyViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val label = passages.toReadingLabel()

    LaunchedEffect(passages, dayRoute, label) {
        viewModel.onEvent(DayStudyUiEvent.OnStart(passages, dayRoute, label))
    }

    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            DayStudyUiAction.NavigateToPaywall -> onOpenPaywall()

            DayStudyUiAction.NavigateToLoginWarning -> onOpenLoginWarning()

            is DayStudyUiAction.ShowSnackBar -> onShowSnackBar(getString(action.message))

            is DayStudyUiAction.ShowSnackBarPlural ->
                onShowSnackBar(getPluralString(action.resource, action.count, action.count))
        }
    }

    if (inline) {
        DayStudyPane(
            cardState = uiState.card,
            openStudy = uiState.openStudy,
            generation = uiState.generation,
            onCardClick = { viewModel.onEvent(DayStudyUiEvent.OnCardClick) },
            modifier = modifier,
        )
        return
    }

    when (val card = uiState.card) {
        is Loadable.Loading -> AiStudyEntryCardSkeleton(modifier = modifier)

        is Loadable.Loaded -> AiStudyEntryCard(
            card = card.value,
            generation = uiState.generation,
            onClick = { viewModel.onEvent(DayStudyUiEvent.OnCardClick) },
            modifier = modifier,
        )
    }

    val isSheetVisible = uiState.isStudyOpen && (uiState.generation != null || uiState.openStudy != null)
    if (isSheetVisible) {
        DayStudySheet(
            study = uiState.openStudy,
            generation = uiState.generation,
            onDismiss = { viewModel.onEvent(DayStudyUiEvent.OnStudyDismiss) },
        )
    }
}
