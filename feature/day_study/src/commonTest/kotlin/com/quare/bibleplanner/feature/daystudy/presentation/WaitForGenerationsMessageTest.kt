package com.quare.bibleplanner.feature.daystudy.presentation

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class WaitForGenerationsMessageTest {
    @Test
    fun `GIVEN several generations in flight WHEN building the message THEN names how many`() = runTest {
        // Given
        val count = 3

        // When
        val message = getWaitForGenerationsMessage(count)

        // Then
        assertTrue("3" in message)
    }

    @Test
    fun `GIVEN one or several generations in flight WHEN building the message THEN uses the matching plural`() =
        runTest {
            // Given
            val single = 1
            val several = 3

            // When
            val singleMessage = getWaitForGenerationsMessage(single)
            val severalMessage = getWaitForGenerationsMessage(several)

            // Then
            assertNotEquals(singleMessage.replace("1", ""), severalMessage.replace("3", ""))
        }
}
