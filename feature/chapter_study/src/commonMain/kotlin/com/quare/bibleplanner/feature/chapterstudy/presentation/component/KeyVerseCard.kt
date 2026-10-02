package com.quare.bibleplanner.feature.chapterstudy.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chapter_study.generated.resources.Res
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_key_verse_caption
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_share
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.core.chapterstudy.domain.model.KeyVerseModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun KeyVerseCard(
    bookId: BookId,
    chapterNumber: Int,
    keyVerse: KeyVerseModel,
    text: String?,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reference = verseReferenceLabel(
        bookId = bookId,
        chapterNumber = chapterNumber,
        verseNumbers = (keyVerse.startVerse..keyVerse.endVerse).toList(),
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 16.dp,
            ),
        ) {
            text?.let { verseText ->
                Text(
                    text = verseText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                )
                VerticalSpacer(9)
            }
            Text(
                text = if (keyVerse.note.isBlank()) {
                    reference
                } else {
                    stringResource(Res.string.chapter_study_key_verse_caption, reference, keyVerse.note)
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            VerticalSpacer(12)
            OutlinedButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                HorizontalSpacer(ButtonDefaults.IconSpacing)
                Text(text = stringResource(Res.string.chapter_study_share))
            }
        }
    }
}
