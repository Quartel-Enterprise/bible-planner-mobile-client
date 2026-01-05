package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.component.centeredItem
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.plan.PassagePlanModel
import com.quare.bibleplanner.core.utils.orFalse

internal fun LazyListScope.portraitPassageList(
    contentMaxWidth: Dp,
    passages: List<PassagePlanModel>,
    chapterReadStatus: Map<Pair<Int, Int>, Boolean>,
    onChapterToggle: (passageIndex: Int, chapterIndex: Int) -> Unit,
) {
    passages.forEachIndexed { passageIndex, passage ->
        val onToggle = { onChapterToggle(passageIndex, -1) }
        val commonModifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
            .padding(vertical = 8.dp)
        if (passage.chapters.isEmpty()) {
            centeredClickableItem(onClick = onToggle, contentMaxWidth = contentMaxWidth) {
                ChapterItemComponent(
                    modifier = commonModifier,
                    bookName = passage.bookId.getBookName(),
                    chapterPlanModel = null,
                    isRead = passage.isRead,
                    onToggle = onToggle,
                )
            }
            if (passageIndex < passages.size - 1) {
                dividerItem(contentMaxWidth = contentMaxWidth)
            }
        } else {
            // Show each chapter as a separate item
            passage.chapters.forEachIndexed { chapterIndex, chapter ->
                val chapterToggle = { onChapterToggle(passageIndex, chapterIndex) }
                centeredClickableItem(onClick = chapterToggle, contentMaxWidth = contentMaxWidth) {
                    ChapterItemComponent(
                        modifier = commonModifier,
                        bookName = passage.bookId.getBookName(),
                        chapterPlanModel = chapter,
                        isRead = chapterReadStatus[passageIndex to chapterIndex].orFalse(),
                        onToggle = chapterToggle,
                    )
                }
                dividerItem(contentMaxWidth = contentMaxWidth)
            }
        }
    }
}

private fun LazyListScope.centeredClickableItem(
    onClick: () -> Unit,
    contentMaxWidth: Dp,
    content: @Composable () -> Unit,
) {
    centeredItem(contentMaxWidth) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        ) {
            content()
        }
    }
}

private fun LazyListScope.dividerItem(contentMaxWidth: Dp) {
    centeredItem(contentMaxWidth) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        )
    }
}
