package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.testing.setUiTestContent
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class ReaderWidthLayoutUiTest {
    private var isReaderWide: Boolean? = null

    @Test
    fun `GIVEN a wide window but a narrow reader pane WHEN rendered THEN lays the reader out narrow`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                isWindowWide = true,
                paneWidth = 300.dp,
            )

            // When
            waitForIdle()

            // Then
            assertEquals(expected = false, actual = isReaderWide)
        }

    @Test
    fun `GIVEN a narrow window WHEN rendered THEN lays the reader out narrow`() = runComposeUiTest {
        // Given
        prepareScenario(
            isWindowWide = false,
            paneWidth = 300.dp,
        )

        // When
        waitForIdle()

        // Then
        assertEquals(expected = false, actual = isReaderWide)
    }

    private fun ComposeUiTest.prepareScenario(
        isWindowWide: Boolean,
        paneWidth: Dp,
    ) {
        isReaderWide = null
        setUiTestContent {
            CompositionLocalProvider(LocalIsWideLayout provides isWindowWide) {
                Box(modifier = Modifier.width(paneWidth)) {
                    ReaderWidthLayout {
                        isReaderWide = LocalIsWideLayout.current
                    }
                }
            }
        }
    }
}
