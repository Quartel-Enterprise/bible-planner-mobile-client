package com.quare.bibleplanner.feature.readingplan.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.delete_progress_option
import bibleplanner.feature.reading_plan.generated.resources.go_to_unread
import bibleplanner.feature.reading_plan.generated.resources.more_options
import bibleplanner.feature.reading_plan.generated.resources.reading_plan
import bibleplanner.feature.reading_plan.generated.resources.scroll_to_top
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
    lazyListState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    // Determine if the first unread week is visible and expanded
    // Hide FAB if we're already viewing the first unread week (expanded and at top)
    val isFirstUnreadWeekVisible = remember(uiState) {
        if (uiState is ReadingPlanUiState.Loaded) {
            val firstUnreadWeek = uiState.weekPlans.find { week ->
                week.weekPlan.days.any { day ->
                    day.passages.any { passage -> !passage.isRead }
                }
            }
            // If the first unread week is expanded and we're at the top, we're "in" it
            firstUnreadWeek != null && firstUnreadWeek.isExpanded && !uiState.isScrolledDown
        } else {
            false
        }
    }

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
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                // Scroll to top FAB - only visible when scrolled down
                AnimatedVisibility(
                    visible = uiState.isScrolledDown,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            onEvent(ReadingPlanUiEvent.OnScrollToTopClick)
                        },
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = stringResource(Res.string.scroll_to_top),
                        )
                    }
                }
                // Main FAB - scroll to first unread week
                // Hide if we're already viewing the first unread week opened
                if (!isFirstUnreadWeekVisible) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            onEvent(ReadingPlanUiEvent.OnScrollToFirstUnreadWeekClick)
                        },
                        expanded = !uiState.isScrolledDown,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                            )
                        },
                        text = {
                            Text(stringResource(Res.string.go_to_unread))
                        },
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
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
                lazyListState = lazyListState,
            )
        }
    }
}
