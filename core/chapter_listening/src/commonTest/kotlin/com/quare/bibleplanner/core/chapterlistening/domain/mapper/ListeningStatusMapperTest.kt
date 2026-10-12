package com.quare.bibleplanner.core.chapterlistening.domain.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioFailureModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ListeningStatusMapperTest {
    @Test
    fun `GIVEN each audio failure WHEN mapping it THEN shows why the chapter cannot play`() {
        // Given
        val failures = ChapterAudioFailureModel.entries

        // When
        val statuses = failures.associateWith(ChapterAudioFailureModel::toListeningStatus)

        // Then
        assertEquals(
            mapOf(
                ChapterAudioFailureModel.NO_CONNECTION to ListeningStatusModel.NEEDS_CONNECTION,
                ChapterAudioFailureModel.NOT_SIGNED_IN to ListeningStatusModel.NEEDS_SIGN_IN,
                ChapterAudioFailureModel.VERSION_NOT_ENABLED to ListeningStatusModel.VERSION_UNAVAILABLE,
                ChapterAudioFailureModel.BUDGET_REACHED to ListeningStatusModel.NARRATION_PAUSED,
                ChapterAudioFailureModel.UNAVAILABLE to ListeningStatusModel.AUDIO_UNAVAILABLE,
            ),
            statuses,
        )
    }
}
