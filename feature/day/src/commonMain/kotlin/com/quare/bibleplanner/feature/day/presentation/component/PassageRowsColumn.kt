package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.utils.orFalse
import com.quare.bibleplanner.feature.day.domain.model.ChapterClickStrategy
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent

@Composable
internal fun PassageRowsColumn(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    passages: List<PassageModel>,
    chapterReadStatus: Map<Pair<Int, Int>, Boolean>,
    itemStartPadding: Dp,
    onEvent: (DayUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemModifier = Modifier
        .fillMaxWidth()
        .padding(start = itemStartPadding)
        .padding(vertical = 8.dp)
    Column(modifier = modifier) {
        passages.forEachIndexed { passageIndex, passage ->
            if (passage.chapters.isEmpty()) {
                PassageRow(
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    contentModifier = itemModifier,
                    passage = passage,
                    chapter = null,
                    isRead = passage.isRead,
                    clickStrategy = ChapterClickStrategy.NavigateToFirstChapterOfTheBook(
                        bookId = passage.bookId,
                        isChapterRead = passage.isRead,
                    ),
                    toggleStrategy = UpdateReadStatusOfPassageStrategy.EntireBook(passageIndex),
                    onEvent = onEvent,
                )
                if (passageIndex < passages.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            } else {
                passage.chapters.forEachIndexed { chapterIndex, chapter ->
                    PassageRow(
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope = animatedContentScope,
                        contentModifier = itemModifier,
                        passage = passage,
                        chapter = chapter,
                        isRead = chapterReadStatus[passageIndex to chapterIndex].orFalse(),
                        clickStrategy = ChapterClickStrategy.NavigateToChapter(
                            bookId = passage.bookId,
                            isChapterRead = passage.isRead,
                            chapterNumber = chapter.number,
                        ),
                        toggleStrategy = UpdateReadStatusOfPassageStrategy.Chapter(
                            passageIndex = passageIndex,
                            chapterIndex = chapterIndex,
                        ),
                        onEvent = onEvent,
                    )
                    val isLastPassage = passageIndex == passages.size - 1
                    val isLastChapter = chapterIndex == passage.chapters.size - 1
                    if (!(isLastPassage && isLastChapter)) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PassageRow(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    passage: PassageModel,
    chapter: ChapterModel?,
    isRead: Boolean,
    clickStrategy: ChapterClickStrategy,
    toggleStrategy: UpdateReadStatusOfPassageStrategy,
    // Why: the padding goes on the chapter item, inside the clickable row, so the ripple spans the whole width.
    contentModifier: Modifier,
    onEvent: (DayUiEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEvent(DayUiEvent.OnChapterClick(clickStrategy)) },
    ) {
        ChapterItemComponent(
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
            modifier = contentModifier,
            bookName = passage.bookId.getBookName(),
            chapterPlanModel = chapter,
            isRead = isRead,
            onToggle = { onEvent(DayUiEvent.OnChapterCheckboxClick(toggleStrategy)) },
        )
    }
}
