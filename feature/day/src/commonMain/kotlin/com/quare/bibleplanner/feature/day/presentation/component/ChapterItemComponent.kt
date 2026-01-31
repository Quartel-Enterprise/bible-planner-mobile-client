package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.vers
import com.quare.bibleplanner.core.model.plan.ChapterModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChapterItemComponent(
    modifier: Modifier = Modifier,
    bookName: String,
    chapterPlanModel: ChapterModel?,
    isRead: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(
                space = 4.dp,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            Text(
                text = formatChapterText(bookName, chapterPlanModel?.number),
                style = MaterialTheme.typography.bodyLarge,
            )
            val startVerse = chapterPlanModel?.startVerse
            val endVerse = chapterPlanModel?.endVerse
            if (startVerse != null && endVerse != null) {
                Text(
                    text = "${stringResource(Res.string.vers)} $startVerse-$endVerse",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Checkbox(
            checked = isRead,
            onCheckedChange = { onToggle() },
        )
    }
}

private fun formatChapterText(
    bookName: String,
    chapterNumber: Int?,
): String = if (chapterNumber == null) {
    bookName
} else {
    "$bookName $chapterNumber"
}
