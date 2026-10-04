package com.quare.bibleplanner.feature.read.presentation.listening

import com.quare.bibleplanner.core.chapterlistening.domain.controller.ChapterListeningController
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningControl
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningSurface
import kotlinx.coroutines.CoroutineScope

internal class ListeningPlaybackActions(
    private val controller: ChapterListeningController,
    private val gate: ChapterListeningGate,
    private val trackEvent: TrackEvent,
) {
    fun togglePlayPause(surface: ListeningSurface) {
        val session = controller.state.value.session ?: return
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_CONTROL_CLICKED,
            params = mapOf(
                AnalyticsParams.CONTROL to toToggleControl(session.status).key,
                AnalyticsParams.SURFACE to surface.key,
            ),
        )
        controller.togglePlayPause()
    }

    fun unlockLockedChapter(scope: CoroutineScope) {
        val locked = controller.state.value.session
            ?.lockedSegment
            ?.chapter ?: return
        gate.request(
            scope = scope,
            chapter = locked,
            onAllowed = controller::continueLockedChapter,
        )
    }

    private fun toToggleControl(status: ListeningStatusModel): ListeningControl = when (status) {
        ListeningStatusModel.PLAYING, ListeningStatusModel.PREPARING -> ListeningControl.PAUSE
        ListeningStatusModel.INTERRUPTED -> ListeningControl.RESUME
        else -> ListeningControl.PLAY
    }
}
