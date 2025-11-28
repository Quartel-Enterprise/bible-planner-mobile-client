package com.quare.bibleplanner.feature.readingplan.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.reading_plan
import com.quare.bibleplanner.feature.readingplan.presentation.content.AnimatedReadingPlanContent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingPlanScreen(
    uiState: ReadingPlanUiState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(Res.string.reading_plan))
                },
            )
        },
    ) { paddingValues ->
        AnimatedReadingPlanContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            uiState = uiState,
            onEvent = onEvent,
        )
    }
}
