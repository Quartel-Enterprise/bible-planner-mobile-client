package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.plan.PassagePlanModel
import com.quare.bibleplanner.core.utils.orFalse

internal fun LazyListScope.passageList(
    passages: List<PassagePlanModel>,
    chapterReadStatus: Map<Pair<Int, Int>, Boolean>,
    onChapterToggle: (passageIndex: Int, chapterIndex: Int) -> Unit,
    maxContentWidth: Dp,
) {
    passages.forEachIndexed { passageIndex, passage ->
        val onToggle = { onChapterToggle(passageIndex, -1) }
        val commonModifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
            .padding(vertical = 8.dp)
        if (passage.chapters.isEmpty()) {
            centeredClickableItem(maxContentWidth, onToggle) {
                ChapterItemComponent(
                    modifier = commonModifier,
                    bookName = passage.bookId.getBookName(),
                    chapterPlanModel = null,
                    isRead = passage.isRead,
                    onToggle = onToggle,
                )
            }
            if (passageIndex < passages.size - 1) {
                dividerItem(maxContentWidth)
            }
        } else {
            // Show each chapter as a separate item
            passage.chapters.forEachIndexed { chapterIndex, chapter ->
                val chapterToggle = { onChapterToggle(passageIndex, chapterIndex) }
                centeredClickableItem(maxContentWidth, chapterToggle) {
                    ChapterItemComponent(
                        modifier = commonModifier,
                        bookName = passage.bookId.getBookName(),
                        chapterPlanModel = chapter,
                        isRead = chapterReadStatus[passageIndex to chapterIndex].orFalse(),
                        onToggle = chapterToggle,
                    )
                }
                dividerItem(maxContentWidth)
            }
        }
    }
}

private fun LazyListScope.centeredClickableItem(
    maxContentWidth: Dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.width(maxContentWidth)) {
                content()
            }
        }
    }
}

private fun LazyListScope.dividerItem(maxContentWidth: Dp) {
    item {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.width(maxContentWidth)) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
internal fun PassageListColumn(
    modifier: Modifier = Modifier,
    passages: List<PassagePlanModel>,
    chapterReadStatus: Map<Pair<Int, Int>, Boolean>,
    onChapterToggle: (passageIndex: Int, chapterIndex: Int) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        passages.forEachIndexed { passageIndex, passage ->
            val onToggle = { onChapterToggle(passageIndex, -1) }
            val commonModifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
                .padding(vertical = 8.dp)
            if (passage.chapters.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle() },
                    ) {
                        ChapterItemComponent(
                            modifier = commonModifier,
                            bookName = passage.bookId.getBookName(),
                            chapterPlanModel = null,
                            isRead = passage.isRead,
                            onToggle = onToggle,
                        )
                    }
                }
                if (passageIndex < passages.size - 1) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            } else {
                // Show each chapter as a separate item
                passage.chapters.forEachIndexed { chapterIndex, chapter ->
                    val chapterToggle = { onChapterToggle(passageIndex, chapterIndex) }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { chapterToggle() },
                        ) {
                            ChapterItemComponent(
                                modifier = commonModifier,
                                bookName = passage.bookId.getBookName(),
                                chapterPlanModel = chapter,
                                isRead = chapterReadStatus[passageIndex to chapterIndex].orFalse(),
                                onToggle = chapterToggle,
                            )
                        }
                    }
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}
