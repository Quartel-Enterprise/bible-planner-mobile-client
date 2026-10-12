package com.quare.bibleplanner.core.chapterlistening.domain.model

data class ChapterListeningSettingsModel(
    val speed: Float,
    val isAutoNextEnabled: Boolean,
    val isWordHighlightEnabled: Boolean,
) {
    companion object {
        const val DEFAULT_SPEED = 1f
    }
}
