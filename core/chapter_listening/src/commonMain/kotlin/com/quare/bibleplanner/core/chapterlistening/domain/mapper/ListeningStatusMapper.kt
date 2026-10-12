package com.quare.bibleplanner.core.chapterlistening.domain.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioFailureModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel

internal fun ChapterAudioFailureModel.toListeningStatus(): ListeningStatusModel = when (this) {
    ChapterAudioFailureModel.NO_CONNECTION -> ListeningStatusModel.NEEDS_CONNECTION
    ChapterAudioFailureModel.NOT_SIGNED_IN -> ListeningStatusModel.NEEDS_SIGN_IN
    ChapterAudioFailureModel.VERSION_NOT_ENABLED -> ListeningStatusModel.VERSION_UNAVAILABLE
    ChapterAudioFailureModel.BUDGET_REACHED -> ListeningStatusModel.NARRATION_PAUSED
    ChapterAudioFailureModel.UNAVAILABLE -> ListeningStatusModel.AUDIO_UNAVAILABLE
}
