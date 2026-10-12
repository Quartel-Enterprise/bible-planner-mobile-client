package com.quare.bibleplanner.core.chapterlistening.domain.model

data class ChapterListeningSettingsModel(
    val speed: Float,
    val isAutoNextEnabled: Boolean,
) {
    companion object {
        const val DEFAULT_SPEED = 1f
    }
}
