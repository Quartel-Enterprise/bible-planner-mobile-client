package com.quare.bibleplanner.feature.dayreadingcomplete.presentation

import bibleplanner.feature.day_reading_complete.generated.resources.Res
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_never_show_confirmation
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_offline_message
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteMessage
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DayReadingCompleteMessageTextTest {
    @Test
    fun `GIVEN each message WHEN mapping it THEN picks the snackbar text for it`() {
        // Given
        val messages = DayReadingCompleteMessage.entries

        // When
        val resources = messages.associateWith(DayReadingCompleteMessage::toStringResource)

        // Then
        assertEquals(
            mapOf(
                DayReadingCompleteMessage.OFFLINE to Res.string.day_reading_complete_offline_message,
                DayReadingCompleteMessage.NEVER_SHOW_CONFIRMATION to
                    Res.string.day_reading_complete_never_show_confirmation,
            ),
            resources,
        )
    }
}
