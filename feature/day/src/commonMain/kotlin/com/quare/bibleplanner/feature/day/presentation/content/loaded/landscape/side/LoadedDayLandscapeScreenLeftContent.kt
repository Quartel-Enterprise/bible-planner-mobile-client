package com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.side

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.utils.orFalse
import com.quare.bibleplanner.feature.day.domain.model.ChapterClickStrategy
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy
import com.quare.bibleplanner.feature.day.presentation.component.ChangeReadStatusButton
import com.quare.bibleplanner.feature.day.presentation.component.ChapterItemComponent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

@Composable
internal fun LoadedDayLandscapeScreenLeftContent(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    day: DayModel,
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
) {
    val isDayRead = day.isRead
    Column(
        modifier = modifier,
    ) {
        ChangeReadStatusButton(
            isDayRead = isDayRead,
            buttonModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            onClick = {
                onEvent(DayUiEvent.OnDayReadToggle)
            },
        )
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
        ) {
            day.passages.forEachIndexed { passageIndex, passage ->
                val commonModifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
                    .padding(vertical = 8.dp)
                if (passage.chapters.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onEvent(
                                    DayUiEvent.OnChapterClick(
                                        ChapterClickStrategy.NavigateToFirstChapterOfTheBook(
                                            bookId = passage.bookId,
                                            isChapterRead = passage.isRead,
                                        ),
                                    ),
                                )
                            },
                    ) {
                        ChapterItemComponent(
                            animatedContentScope = animatedContentScope,
                            sharedTransitionScope = sharedTransitionScope,
                            modifier = commonModifier,
                            bookName = passage.bookId.getBookName(),
                            chapterPlanModel = null,
                            isRead = passage.isRead,
                            onToggle = {
                                onEvent(
                                    DayUiEvent.OnChapterCheckboxClick(
                                        UpdateReadStatusOfPassageStrategy.EntireBook(passageIndex),
                                    ),
                                )
                            },
                        )
                    }
                    if (passageIndex < day.passages.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                } else {
                    // Show each chapter as a separate item
                    passage.chapters.forEachIndexed { chapterIndex, chapter ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEvent(
                                        DayUiEvent.OnChapterClick(
                                            ChapterClickStrategy.NavigateToChapter(
                                                bookId = passage.bookId,
                                                isChapterRead = passage.isRead,
                                                chapterNumber = passage.chapters[chapterIndex].number,
                                            ),
                                        ),
                                    )
                                },
                        ) {
                            ChapterItemComponent(
                                animatedContentScope = animatedContentScope,
                                sharedTransitionScope = sharedTransitionScope,
                                modifier = commonModifier,
                                bookName = passage.bookId.getBookName(),
                                chapterPlanModel = chapter,
                                isRead = uiState.chapterReadStatus[passageIndex to chapterIndex].orFalse(),
                                onToggle = {
                                    onEvent(
                                        DayUiEvent.OnChapterCheckboxClick(
                                            UpdateReadStatusOfPassageStrategy.Chapter(
                                                passageIndex = passageIndex,
                                                chapterIndex = chapterIndex,
                                            ),
                                        ),
                                    )
                                },
                            )
                        }

                        val isLastPassage = passageIndex == day.passages.size - 1
                        val isLastChapter = chapterIndex == passage.chapters.size - 1

                        if (!(isLastPassage && isLastChapter)) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}
