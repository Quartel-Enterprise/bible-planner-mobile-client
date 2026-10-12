package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toListeningAnalyticsParams
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioResultModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent

internal class ChapterListeningAnalyticsTracker(
    private val trackEvent: TrackEvent,
) {
    fun trackOpened(
        session: ListeningSessionModel,
        result: ChapterAudioResultModel,
        status: ListeningStatusModel,
    ) {
        val unavailableReason = when {
            result is ChapterAudioResultModel.Failed -> result.failure.name.lowercase()
            status == ListeningStatusModel.AUDIO_UNAVAILABLE -> NO_VERSES_REASON
            else -> null
        }
        if (unavailableReason != null) {
            trackAudioUnavailable(
                session = session,
                reason = unavailableReason,
            )
        }
    }

    fun trackPlaybackFailed(session: ListeningSessionModel) {
        trackAudioUnavailable(
            session = session,
            reason = PLAYBACK_FAILED_REASON,
        )
    }

    fun trackStarted(session: ListeningSessionModel) {
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_STARTED,
            params =
                toChapterParams(session) + mapOf(AnalyticsParams.CAUSE to session.chapterChangeCause.name.lowercase()),
        )
    }

    fun trackCompleted(session: ListeningSessionModel) {
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_COMPLETED,
            params = toChapterParams(session),
        )
    }

    fun trackLocked(segment: ListeningSegmentModel) {
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_LOCKED,
            params = segment.chapter.toListeningAnalyticsParams(),
        )
    }

    fun trackInterrupted(session: ListeningSessionModel) {
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_INTERRUPTED,
            params = toChapterParams(session),
        )
    }

    fun trackSleepTimerEnded(option: String) {
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_SLEEP_TIMER_ENDED,
            params = mapOf(AnalyticsParams.OPTION to option),
        )
    }

    private fun trackAudioUnavailable(
        session: ListeningSessionModel,
        reason: String,
    ) {
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_AUDIO_UNAVAILABLE,
            params = toChapterParams(session) + mapOf(AnalyticsParams.REASON to reason),
        )
    }

    private fun toChapterParams(session: ListeningSessionModel): Map<String, Any> =
        mapOf(AnalyticsParams.MODE to session.mode.key) + session.segment.chapter.toListeningAnalyticsParams()

    private companion object {
        const val NO_VERSES_REASON = "no_verses"
        const val PLAYBACK_FAILED_REASON = "playback_failed"
    }
}
