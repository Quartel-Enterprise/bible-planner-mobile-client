package com.quare.bibleplanner.ui.utils.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.testing.setUiTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class BlockPointerInputUiTest {
    private var clicks = 0

    @Test
    fun `GIVEN input is blocked WHEN tapping a button inside THEN the button never hears it`() = runComposeUiTest {
        // Given
        prepareScenario(isBlocked = true)

        // When
        onNodeWithTag(BUTTON_TAG).performClick()

        // Then
        assertEquals(0, clicks)
    }

    @Test
    fun `GIVEN input is not blocked WHEN tapping a button inside THEN the button hears it`() = runComposeUiTest {
        // Given
        prepareScenario(isBlocked = false)

        // When
        onNodeWithTag(BUTTON_TAG).performClick()

        // Then
        assertEquals(1, clicks)
    }

    private fun ComposeUiTest.prepareScenario(isBlocked: Boolean) {
        setUiTestContent {
            Box(modifier = Modifier.blockPointerInput(isBlocked = isBlocked)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(BUTTON_TAG)
                        .clickable { clicks++ },
                )
            }
        }
    }

    private companion object {
        const val BUTTON_TAG = "button"
    }
}
