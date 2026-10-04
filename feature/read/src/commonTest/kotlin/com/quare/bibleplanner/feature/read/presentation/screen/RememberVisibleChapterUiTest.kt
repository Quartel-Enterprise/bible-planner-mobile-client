package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasScrollToKeyAction
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.read.fixture.hiddenListeningUiState
import com.quare.bibleplanner.feature.read.fixture.readChapter
import com.quare.bibleplanner.feature.read.fixture.readUiState
import com.quare.bibleplanner.feature.read.presentation.component.VerseFlash
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.screen.content.chapterContent
import com.quare.bibleplanner.ui.testing.setUiTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class RememberVisibleChapterUiTest {
    private var visibleChapter: ReadChapterUiModel? = null

    private val listHeight = 120.dp
    private val uiState = readUiState(LOCALE)
    private val chapters = listOf(
        readChapter(
            chapterNumber = 1,
            verseCount = 3,
        ),
        readChapter(
            chapterNumber = 2,
            verseCount = 4,
        ),
        readChapter(
            chapterNumber = 3,
            verseCount = 5,
        ),
    )

    @Test
    fun `GIVEN the study card below WHEN scrolling to the third header THEN the third chapter is visible`() =
        runComposeUiTest {
            // Given
            prepareScenario(isChapterStudyBeside = false)

            // When
            scrollToHeaderOf(chapterNumber = 3)

            // Then
            assertEquals(
                expected = chapters[2],
                actual = visibleChapter,
            )
        }

    @Test
    fun `GIVEN the study card beside WHEN scrolling to the third header THEN the third chapter is visible`() =
        runComposeUiTest {
            // Given
            prepareScenario(isChapterStudyBeside = true)

            // When
            scrollToHeaderOf(chapterNumber = 3)

            // Then
            assertEquals(
                expected = chapters[2],
                actual = visibleChapter,
            )
        }

    private fun ComposeUiTest.scrollToHeaderOf(chapterNumber: Int) {
        onNode(hasScrollToKeyAction()).performScrollToKey("chapter-header-GEN-$chapterNumber")
        waitForIdle()
    }

    private fun ComposeUiTest.prepareScenario(isChapterStudyBeside: Boolean) {
        visibleChapter = null
        setUiTestContent {
            val listState = rememberLazyListState()
            val currentVisibleChapter = rememberVisibleChapter(
                chapters = chapters,
                listState = listState,
                leadingItemCount = 0,
                isChapterStudyBeside = isChapterStudyBeside,
            )
            SideEffect { visibleChapter = currentVisibleChapter }
            LazyColumn(
                modifier = Modifier.height(listHeight),
                state = listState,
            ) {
                chapters.forEach { chapter ->
                    chapterContent(
                        chapter = chapter,
                        header = uiState.header,
                        settings = uiState.settings,
                        isChapterStudyBeside = isChapterStudyBeside,
                        focusedVerseNumber = null,
                        verseFlash = VerseFlash(
                            focus = null,
                            alpha = { 0f },
                        ),
                        listening = hiddenListeningUiState(),
                        onEvent = {},
                        onListeningEvent = {},
                    )
                }
            }
        }
    }

    private companion object {
        const val LOCALE = "en-US"
    }
}
