package com.quare.bibleplanner.feature.chapterstudy.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chapter_study.generated.resources.Res
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_context
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_cross_references
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_key_verse
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_outline
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_people_and_places
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_reflection
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_summary
import com.quare.bibleplanner.core.books.util.toVerseNumbersLabel
import com.quare.bibleplanner.core.chapterstudy.domain.model.KeyVerseModel
import com.quare.bibleplanner.feature.chapterstudy.presentation.component.ChapterStudySectionLabel
import com.quare.bibleplanner.feature.chapterstudy.presentation.component.CrossReferenceChip
import com.quare.bibleplanner.feature.chapterstudy.presentation.component.KeyVerseCard
import com.quare.bibleplanner.feature.chapterstudy.presentation.component.NameChip
import com.quare.bibleplanner.feature.chapterstudy.presentation.component.OutlineSectionRow
import com.quare.bibleplanner.feature.chapterstudy.presentation.component.ReflectionQuestionCard
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyContentUiState
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiEvent
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiState
import com.quare.bibleplanner.ui.component.ExpandableText
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.component.study.AiDisclaimerText
import org.jetbrains.compose.resources.stringResource

private val contentMaxWidth = 720.dp
private val contentPadding = PaddingValues(
    start = 18.dp,
    top = 12.dp,
    end = 18.dp,
    bottom = 96.dp,
)
private val chipSpacing = 6.dp
private val rowSpacing = 6.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ChapterStudyLoadedContent(
    uiState: ChapterStudyUiState,
    content: ChapterStudyContentUiState.Loaded,
    onEvent: (ChapterStudyUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val study = content.study
    Column(
        modifier = modifier
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
            ChapterStudySectionLabel(
                text = stringResource(Res.string.chapter_study_section_summary),
                isFirst = true,
            )
            ExpandableText(
                text = study.summary,
                style = MaterialTheme.typography.bodyMedium,
            )
            ChapterStudySectionLabel(text = stringResource(Res.string.chapter_study_section_context))
            Text(
                text = study.context,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (study.outline.isNotEmpty()) {
                ChapterStudySectionLabel(text = stringResource(Res.string.chapter_study_section_outline))
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
                ChapterStudySectionLabel(text = stringResource(Res.string.chapter_study_section_people_and_places))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(chipSpacing),
                    verticalArrangement = Arrangement.spacedBy(chipSpacing),
                ) {
                    study.peopleAndPlaces.forEach { name -> NameChip(name = name) }
                }
            }
            study.keyVerse?.let { keyVerse ->
                ChapterStudySectionLabel(
                    text = stringResource(
                        Res.string.chapter_study_section_key_verse,
                        keyVerse.toShortReference(uiState.chapterNumber),
                    ),
                )
                KeyVerseCard(
                    bookId = uiState.bookId,
                    chapterNumber = uiState.chapterNumber,
                    keyVerse = keyVerse,
                    text = content.keyVerseText,
                    onShareClick = { onEvent(ChapterStudyUiEvent.OnShareKeyVerseClick) },
                )
            }
            if (study.crossReferences.isNotEmpty()) {
                ChapterStudySectionLabel(text = stringResource(Res.string.chapter_study_section_cross_references))
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
                ChapterStudySectionLabel(text = stringResource(Res.string.chapter_study_section_reflection))
                Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
                    study.reflectionQuestions.forEach { question -> ReflectionQuestionCard(question = question) }
                }
            }
            VerticalSpacer(16)
            AiDisclaimerText()
        }
    }
}

private fun KeyVerseModel.toShortReference(chapterNumber: Int): String =
    "$chapterNumber:${(startVerse..endVerse).toList().toVerseNumbersLabel()}"
