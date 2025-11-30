package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.day_week_title
import bibleplanner.feature.day.generated.resources.mark_as_read
import bibleplanner.feature.day.generated.resources.mark_as_unread
import com.quare.bibleplanner.feature.day.presentation.component.DayProgress
import com.quare.bibleplanner.feature.day.presentation.content.DayContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.component.backicon.BackIcon
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

private const val MAX_CONTENT_WIDTH = 600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayScreen(
    uiState: DayUiState,
    onEvent: (DayUiEvent) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    (uiState as? DayUiState.Loaded)?.run {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(
                                    Res.string.day_week_title,
                                    day.number,
                                    weekNumber,
                                ),
                            )
                            VerticalSpacer(4)
                            DayProgress(
                                passages = day.passages,
                                books = books,
                            )
                        }
                    }
                },
                navigationIcon = {
                    BackIcon(onBackClick = { onEvent(DayUiEvent.OnBackClick) })
                },
                actions = {
                    (uiState as? DayUiState.Loaded)?.run {
                        OutlinedButton(
                            onClick = {
                                onEvent(
                                    DayUiEvent.OnDayReadToggle(!day.isRead),
                                )
                            },
                        ) {
                            Text(
                                text = stringResource(
                                    if (day.isRead) Res.string.mark_as_unread else Res.string.mark_as_read,
                                ),
                            )
                        }
                    }
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
            DayContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onEvent = onEvent,
                maxContentWidth = constrainedWidth,
            )
        }
    }
}
