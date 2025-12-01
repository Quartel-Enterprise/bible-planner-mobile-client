package com.quare.bibleplanner.feature.readingplan.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.delete_progress_option
import bibleplanner.feature.reading_plan.generated.resources.more_options
import bibleplanner.feature.reading_plan.generated.resources.reading_plan
import bibleplanner.feature.reading_plan.generated.resources.theme_option
import com.quare.bibleplanner.feature.readingplan.presentation.content.ReadingPlanContent
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOption
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.stringResource

private const val MAX_CONTENT_WIDTH = 600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingPlanScreen(
    uiState: ReadingPlanUiState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    scrollToWeekNumber: Int,
    onScrollToWeekCompleted: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = stringResource(Res.string.reading_plan))
                    }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    CommonIconButton(
                        imageVector = Icons.Default.MoreVert,
                        onClick = {
                            onEvent(ReadingPlanUiEvent.OnOverflowClick)
                        },
                        contentDescription = stringResource(Res.string.more_options),
                    )
                    DropdownMenu(
                        expanded = uiState.isShowingMenu,
                        onDismissRequest = {
                            onEvent(ReadingPlanUiEvent.OnOverflowDismiss)
                        },
                    ) {
                        val themeTitle = stringResource(Res.string.theme_option)
                        val deleteProgressTitle = stringResource(Res.string.delete_progress_option)
                        val errorColor = MaterialTheme.colorScheme.error
                        DropdownMenuItem(
                            text = {
                                Text(themeTitle)
                            },
                            onClick = {
                                onEvent(ReadingPlanUiEvent.OnOverflowOptionClick(OverflowOption.THEME))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Contrast,
                                    contentDescription = themeTitle,
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = deleteProgressTitle,
                                    color = errorColor,
                                )
                            },
                            onClick = {
                                onEvent(ReadingPlanUiEvent.OnOverflowOptionClick(OverflowOption.DELETE_PROGRESS))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = deleteProgressTitle,
                                    tint = errorColor,
                                )
                            },
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val constrainedWidth = maxWidth.coerceAtMost(MAX_CONTENT_WIDTH.dp)
            ReadingPlanContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onEvent = onEvent,
                maxContentWidth = constrainedWidth,
                scrollToWeekNumber = scrollToWeekNumber,
                onScrollToWeekCompleted = onScrollToWeekCompleted,
            )
        }
    }
}
