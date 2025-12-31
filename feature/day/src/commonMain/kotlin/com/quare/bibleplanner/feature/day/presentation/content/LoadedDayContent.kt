package com.quare.bibleplanner.feature.day.presentation.content

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.feature.day.presentation.component.ChangeReadStatusButton
import com.quare.bibleplanner.feature.day.presentation.component.DayReadSection
import com.quare.bibleplanner.feature.day.presentation.component.PassageListColumn
import com.quare.bibleplanner.feature.day.presentation.component.notes.NotesSection
import com.quare.bibleplanner.feature.day.presentation.component.passageList
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

private const val WIDE_SCREEN_BREAKPOINT = 840
private const val MAX_CONTENT_WIDTH = 1200

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun LoadedDayContent(
    modifier: Modifier = Modifier,
    maxContentWidth: Dp,
    uiState: DayUiState.Loaded,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onEvent: (DayUiEvent) -> Unit,
) {
    val day = uiState.day
    BoxWithConstraints(modifier = modifier) {
        val isWideScreen = maxWidth > WIDE_SCREEN_BREAKPOINT.dp
        val contentMaxWidth = maxWidth.coerceAtMost(MAX_CONTENT_WIDTH.dp)

        if (isWideScreen) {
            // Two-column layout for wide screens
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.widthIn(max = contentMaxWidth),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Left column: Passages
                    Box(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight(),
                    ) {
                        PassageListColumn(
                            modifier = Modifier.fillMaxWidth(),
                            passages = day.passages,
                            chapterReadStatus = uiState.chapterReadStatus,
                            onChapterToggle = { passageIndex, chapterIndex ->
                                onEvent(DayUiEvent.OnChapterToggle(passageIndex, chapterIndex))
                            },
                        )
                    }

                    // Right column: Other content
                    Box(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight(),
                    ) {
                        RightColumnContent(
                            day = day,
                            uiState = uiState,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedContentScope = animatedContentScope,
                            onEvent = onEvent,
                        )
                    }
                }
            }
        } else {
            // Single column layout for narrow screens
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                val isDayRead = day.isRead
                centeredContentItem(maxContentWidth) {
                    ChangeReadStatusButton(
                        isDayRead = isDayRead,
                        buttonModifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp,
                            ),
                        onClick = {
                            onEvent(DayUiEvent.OnDayReadToggle)
                        },
                    )
                }

                passageList(
                    passages = day.passages,
                    chapterReadStatus = uiState.chapterReadStatus,
                    onChapterToggle = { passageIndex, chapterIndex ->
                        onEvent(DayUiEvent.OnChapterToggle(passageIndex, chapterIndex))
                    },
                    maxContentWidth = maxContentWidth,
                )

                day.plannedReadDate?.let { plannedReadDate ->
                    centeredContentItem(maxContentWidth) {
                        with(sharedTransitionScope) {
                            PlannedReadDateComponent(
                                modifier = Modifier.padding(8.dp),
                                plannedReadDate = plannedReadDate,
                                animatedContentScope = animatedContentScope,
                                weekNumber = uiState.weekNumber,
                                dayNumber = day.number,
                            )
                        }
                    }
                }

                centeredContentItem(maxContentWidth) {
                    DayReadSection(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        isRead = isDayRead,
                        formattedReadDate = uiState.formattedReadDate,
                        onEvent = onEvent,
                    )
                }

                centeredContentItem(maxContentWidth) {
                    NotesSection(
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                        notesText = uiState.notesText,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RightColumnContent(
    day: DayModel,
    uiState: DayUiState.Loaded,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onEvent: (DayUiEvent) -> Unit,
) {
    val isDayRead = day.isRead
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
    ) {
        item {
            ChangeReadStatusButton(
                isDayRead = isDayRead,
                buttonModifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
                onClick = {
                    onEvent(DayUiEvent.OnDayReadToggle)
                },
            )
        }

        day.plannedReadDate?.let { plannedReadDate ->
            item {
                with(sharedTransitionScope) {
                    PlannedReadDateComponent(
                        modifier = Modifier.padding(8.dp),
                        plannedReadDate = plannedReadDate,
                        animatedContentScope = animatedContentScope,
                        weekNumber = uiState.weekNumber,
                        dayNumber = day.number,
                    )
                }
            }
        }

        item {
            DayReadSection(
                modifier = Modifier.padding(horizontal = 8.dp),
                isRead = isDayRead,
                formattedReadDate = uiState.formattedReadDate,
                onEvent = onEvent,
            )
        }

        item {
            NotesSection(
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                notesText = uiState.notesText,
                onEvent = onEvent,
            )
        }
    }
}

private fun LazyListScope.centeredContentItem(
    maxContentWidth: Dp,
    content: @Composable () -> Unit,
) {
    item {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.width(maxContentWidth)) {
                content()
            }
        }
    }
}
