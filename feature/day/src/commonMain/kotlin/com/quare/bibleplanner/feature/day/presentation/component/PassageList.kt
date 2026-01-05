package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.plan.PassagePlanModel
import com.quare.bibleplanner.core.utils.orFalse
import com.quare.bibleplanner.ui.component.ResponsiveContentScope

internal fun ResponsiveContentScope.portraitPassageList(
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
            responsiveItem(
                key = "passage_$passageIndex",
                content = {
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
                },
            )
            if (passageIndex < passages.size - 1) {
                dividerItem(passageIndex)
            }
        } else {
            // Show each chapter as a separate item
            passage.chapters.forEachIndexed { chapterIndex, chapter ->
                val chapterToggle = { onChapterToggle(passageIndex, chapterIndex) }
                responsiveItem(
                    key = "passage_${passageIndex}_chapter_$chapterIndex",
                    content = {
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
                    },
                )
                dividerItem(passageIndex, chapterIndex)
            }
        }
    }
}

private fun ResponsiveContentScope.dividerItem(
    passageIndex: Int,
    chapterIndex: Int? = null,
) {
    responsiveItem(
        key = "divider_${passageIndex}${chapterIndex?.let { "_$it" } ?: ""}",
    ) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        )
    }
}
