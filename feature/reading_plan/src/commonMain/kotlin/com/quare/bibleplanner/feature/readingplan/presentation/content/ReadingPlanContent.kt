package com.quare.bibleplanner.feature.readingplan.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanProgress
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanTypesSegmentedButtons
import com.quare.bibleplanner.feature.readingplan.presentation.component.WeekPlanItem
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState

@Composable
internal fun ReadingPlanContent(
    modifier: Modifier = Modifier,
    uiState: ReadingPlanUiState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val loadedUiState = uiState as? ReadingPlanUiState.Loaded
    val isLoading = uiState is ReadingPlanUiState.Loading

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
        }
        item {
            PlanProgress(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                progress = loadedUiState?.progress ?: 0f,
                isLoading = isLoading,
            )
        }
        when (uiState) {
            is ReadingPlanUiState.Loaded -> {
                val selectedWeeks = when (uiState.selectedReadingPlan) {
                    ReadingPlanType.CHRONOLOGICAL -> uiState.plansModel.chronologicalOrder
                    ReadingPlanType.BOOKS -> uiState.plansModel.booksOrder
                }

                items(
                    items = selectedWeeks,
                    key = { week -> week.number },
                ) { week ->
                    WeekPlanItem(
                        modifier = Modifier.fillMaxWidth(),
                        week = week,
                    )
                }
            }

            is ReadingPlanUiState.Loading -> {
                item { LoadingReadingPlanContent(modifier = Modifier.fillMaxWidth()) }
            }
        }
    }
}
