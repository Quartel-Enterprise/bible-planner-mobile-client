package com.quare.bibleplanner.feature.readingplan.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanProgress
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanTypesSegmentedButtons
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
                progress = loadedUiState?.data?.progress ?: 0f,
                isLoading = isLoading
            )
        }
        item {
            when (uiState) {
                is ReadingPlanUiState.Loaded -> Unit
                is ReadingPlanUiState.Loading -> LoadingReadingPlanContent(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
