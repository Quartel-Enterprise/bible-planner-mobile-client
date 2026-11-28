package com.quare.bibleplanner.android.feature.chapterstudy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quare.bibleplanner.android.designsystem.study.AiDisclaimerText
import com.quare.bibleplanner.android.designsystem.study.ExpandableText
import com.quare.bibleplanner.core.books.util.toVerseNumbersLabel
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.core.chapterstudy.domain.model.CrossReferenceModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.KeyVerseModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.OutlineSectionModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyContentUiState
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiEvent
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiState

private val contentMaxWidth = 720.dp
private val contentPadding = PaddingValues(
    start = 18.dp,
    top = 12.dp,
    end = 18.dp,
    bottom = 96.dp,
)
private val chipSpacing = 6.dp
private val rowSpacing = 6.dp

@Composable
internal fun ChapterStudyLoadedContent(
    state: ChapterStudyUiState,
    content: ChapterStudyContentUiState.Loaded,
    onEvent: (ChapterStudyUiEvent) -> Unit,
) {
    val study = content.study
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = contentMaxWidth)
                .fillMaxWidth()
                .padding(contentPadding),
        ) {
            SectionLabel(
                text = stringResource(R.string.chapter_study_section_summary),
                isFirst = true,
            )
            ExpandableText(
                text = study.summary,
                style = MaterialTheme.typography.bodyMedium,
            )
            SectionLabel(text = stringResource(R.string.chapter_study_section_context))
            Text(
                text = study.context,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (study.outline.isNotEmpty()) {
                SectionLabel(text = stringResource(R.string.chapter_study_section_outline))
                Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
                    study.outline.forEach { section ->
                        OutlineSectionRow(
                            section = section,
                            onClick = { onEvent(ChapterStudyUiEvent.OnOutlineSectionClick(section)) },
                        )
                    }
                }
            }
            if (study.peopleAndPlaces.isNotEmpty()) {
                SectionLabel(text = stringResource(R.string.chapter_study_section_people_and_places))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(chipSpacing),
                    verticalArrangement = Arrangement.spacedBy(chipSpacing),
                ) {
                    study.peopleAndPlaces.forEach { name -> NameChip(name = name) }
                }
            }
            study.keyVerse?.let { keyVerse ->
                SectionLabel(
                    text = stringResource(
                        R.string.chapter_study_section_key_verse,
                        keyVerse.toShortReference(state.chapterNumber),
                    ),
                )
                KeyVerseCard(
                    bookId = state.bookId,
                    chapterNumber = state.chapterNumber,
                    keyVerse = keyVerse,
                    text = content.keyVerseText,
                    onShareClick = { onEvent(ChapterStudyUiEvent.OnShareKeyVerseClick) },
                )
            }
            if (study.crossReferences.isNotEmpty()) {
                SectionLabel(text = stringResource(R.string.chapter_study_section_cross_references))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(chipSpacing),
                    verticalArrangement = Arrangement.spacedBy(chipSpacing),
                ) {
                    study.crossReferences.forEach { reference ->
                        CrossReferenceChip(
                            reference = reference,
                            onClick = { onEvent(ChapterStudyUiEvent.OnCrossReferenceClick(reference)) },
                        )
                    }
                }
            }
            if (study.reflectionQuestions.isNotEmpty()) {
                SectionLabel(text = stringResource(R.string.chapter_study_section_reflection))
                Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
                    study.reflectionQuestions.forEach { question -> ReflectionQuestionCard(question = question) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AiDisclaimerText()
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    isFirst: Boolean = false,
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(
                top = if (isFirst) 0.dp else 18.dp,
                bottom = 8.dp,
            ).semantics { heading() },
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun OutlineSectionRow(
    section: OutlineSectionModel,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 11.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = (section.startVerse..section.endVerse).toList().toVerseNumbersLabel(),
                modifier = Modifier.widthIn(min = 38.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = section.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun NameChip(name: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(
                horizontal = 13.dp,
                vertical = 7.dp,
            ),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun CrossReferenceChip(
    reference: CrossReferenceModel,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
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

@Composable
private fun KeyVerseCard(
    bookId: BookId,
    chapterNumber: Int,
    keyVerse: KeyVerseModel,
    text: String?,
    onShareClick: () -> Unit,
) {
    val reference = verseReferenceLabel(
        bookId = bookId,
        chapterNumber = chapterNumber,
        verseNumbers = (keyVerse.startVerse..keyVerse.endVerse).toList(),
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                Spacer(modifier = Modifier.height(9.dp))
            }
            Text(
                text = if (keyVerse.note.isBlank()) {
                    reference
                } else {
                    stringResource(R.string.chapter_study_key_verse_caption, reference, keyVerse.note)
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = stringResource(R.string.chapter_study_share))
            }
        }
    }
}

@Composable
private fun ReflectionQuestionCard(question: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 12.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun KeyVerseModel.toShortReference(chapterNumber: Int): String =
    "$chapterNumber:${(startVerse..endVerse).toList().toVerseNumbersLabel()}"
