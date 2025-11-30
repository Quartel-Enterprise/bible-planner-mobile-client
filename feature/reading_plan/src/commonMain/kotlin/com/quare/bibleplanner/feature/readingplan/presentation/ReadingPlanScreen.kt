package com.quare.bibleplanner.feature.readingplan.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.reading_plan
import com.quare.bibleplanner.feature.readingplan.presentation.content.ReadingPlanContent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import org.jetbrains.compose.resources.stringResource

private const val MAX_CONTENT_WIDTH = 600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingPlanScreen(
    uiState: ReadingPlanUiState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(Res.string.reading_plan))
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val constrainedWidth = maxWidth.coerceAtMost(MAX_CONTENT_WIDTH.dp)
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            ) {
                ReadingPlanContent(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(constrainedWidth),
                    uiState = uiState,
                    onEvent = onEvent,
                )
            }
        }
    }
}
