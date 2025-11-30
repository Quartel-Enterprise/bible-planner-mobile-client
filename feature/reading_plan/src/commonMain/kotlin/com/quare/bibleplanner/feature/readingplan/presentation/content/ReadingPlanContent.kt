package com.quare.bibleplanner.feature.readingplan.presentation.content

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanProgress
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanTypesSegmentedButtons
import com.quare.bibleplanner.feature.readingplan.presentation.component.WeekPlanItem
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@Composable
internal fun ReadingPlanContent(
    modifier: Modifier = Modifier,
    uiState: ReadingPlanUiState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val loadedUiState = uiState as? ReadingPlanUiState.Loaded

    LazyColumn(
        modifier = modifier,
    ) {
        item {
            PlanTypesSegmentedButtons(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                selectedReadingPlan = uiState.selectedReadingPlan,
                onPlanClick = {
                    onEvent(ReadingPlanUiEvent.OnPlanClick(it))
                },
            )
            VerticalSpacer()
        }
        item {
            PlanProgress(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                progress = loadedUiState?.progress ?: 0f,
                isLoading = uiState is ReadingPlanUiState.Loading,
            )
            VerticalSpacer()
        }
        when (uiState) {
            is ReadingPlanUiState.Loaded -> items(
                items = uiState.weekPlans,
                key = { weekPresentation -> weekPresentation.weekPlan.number },
            ) { weekPresentation ->
                WeekPlanItem(
                    modifier = Modifier.fillMaxWidth(),
                    weekPresentation = weekPresentation,
                    onEvent = onEvent,
                )
            }

            is ReadingPlanUiState.Loading -> item {
                LoadingReadingPlanContent(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
