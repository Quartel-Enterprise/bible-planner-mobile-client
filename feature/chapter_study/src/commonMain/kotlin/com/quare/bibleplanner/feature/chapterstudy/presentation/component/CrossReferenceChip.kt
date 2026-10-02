package com.quare.bibleplanner.feature.chapterstudy.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.core.chapterstudy.domain.model.CrossReferenceModel

@Composable
internal fun CrossReferenceChip(
    reference: CrossReferenceModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Text(
            text = verseReferenceLabel(
                bookId = reference.bookId,
                chapterNumber = reference.chapterNumber,
                verseNumbers = (reference.startVerse..reference.endVerse).toList(),
            ),
            modifier = Modifier.padding(
                horizontal = 13.dp,
                vertical = 7.dp,
            ),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
