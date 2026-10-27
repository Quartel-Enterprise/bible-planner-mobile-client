package com.quare.bibleplanner.feature.verse.share.presentation.mapper

import bibleplanner.feature.verse.share.generated.resources.Res
import bibleplanner.feature.verse.share.generated.resources.share_image_save_failed
import bibleplanner.feature.verse.share.generated.resources.share_image_saved
import com.quare.bibleplanner.feature.verse.share.presentation.model.ImageSaveResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ImageSaveResultMapperTest {
    @Test
    fun `GIVEN a saved image WHEN mapping the result THEN confirms the save`() {
        // Given
        val result = ImageSaveResult.SAVED

        // When
        val message = result.toMessage()

        // Then
        assertEquals(
            expected = Res.string.share_image_saved,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a failed save WHEN mapping the result THEN reports the failure`() {
        // Given
        val result = ImageSaveResult.FAILED

        // When
        val message = result.toMessage()

        // Then
        assertEquals(
            expected = Res.string.share_image_save_failed,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a cancelled save WHEN mapping the result THEN shows nothing`() {
        // Given
        val result = ImageSaveResult.CANCELLED

        // When
        val message = result.toMessage()

        // Then
        assertNull(message)
    }
}
