package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
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
    val commonModifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp)
        .padding(vertical = 8.dp)
    responsiveItem(key = "passage_list_card") {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                passages.forEachIndexed { passageIndex, passage ->
                    val onToggle = { onChapterToggle(passageIndex, -1) }
                    if (passage.chapters.isEmpty()) {
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
                        if (passageIndex < passages.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    } else {
                        // Show each chapter as a separate item
                        passage.chapters.forEachIndexed { chapterIndex, chapter ->
                            val chapterToggle = { onChapterToggle(passageIndex, chapterIndex) }
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
    }
}
