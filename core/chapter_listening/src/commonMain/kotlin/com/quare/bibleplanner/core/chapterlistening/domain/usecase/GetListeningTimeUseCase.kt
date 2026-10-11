package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningTimeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import kotlin.time.Duration

class GetListeningTimeUseCase {
    operator fun invoke(
        verses: List<ListeningVerseModel>,
        verseIndex: Int,
        verseProgress: Float,
        speed: Float,
    ): ListeningTimeModel {
        val durations = verses.map { verse -> verse.end - verse.start }
        val before = durations.take(verseIndex).fold(Duration.ZERO, Duration::plus)
        val current = durations.getOrElse(verseIndex) { Duration.ZERO } * verseProgress.coerceIn(0f, 1f).toDouble()
        return ListeningTimeModel(
            elapsed = (before + current) / speed.toDouble(),
            total = durations.fold(Duration.ZERO, Duration::plus) / speed.toDouble(),
        )
    }
}
