package com.quare.bibleplanner.core.chapterlistening.domain.model

sealed interface ChapterAudioResultModel {
    data class Ready(
        val audio: ChapterAudioModel,
    ) : ChapterAudioResultModel

    data class Failed(
        val failure: ChapterAudioFailureModel,
    ) : ChapterAudioResultModel
}
