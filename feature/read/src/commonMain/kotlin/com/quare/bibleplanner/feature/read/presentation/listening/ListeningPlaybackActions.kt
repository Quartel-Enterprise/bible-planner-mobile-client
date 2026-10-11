package com.quare.bibleplanner.feature.read.presentation.listening

import com.quare.bibleplanner.core.chapterlistening.domain.controller.ChapterListeningController
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningControl
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningSurface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class ListeningPlaybackActions(
    private val controller: ChapterListeningController,
    private val gate: ChapterListeningGate,
    private val navigator: Navigator,
    private val trackEvent: TrackEvent,
    private val getAuthenticatedUserId: GetAuthenticatedUserId,
) {
    fun togglePlayPause(surface: ListeningSurface) {
        val session = controller.state.value.session ?: return
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_CONTROL_CLICKED,
            params = mapOf(
                AnalyticsParams.CONTROL to toToggleControl(session).key,
                AnalyticsParams.SURFACE to surface.key,
            ),
        )
        controller.togglePlayPause()
    }

    fun unlockLockedChapter(
        scope: CoroutineScope,
        surface: ListeningSurface,
    ) {
        val locked = controller.state.value.session
            ?.lockedSegment
            ?.chapter ?: return
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_UNLOCK_CLICKED,
            params = mapOf(AnalyticsParams.SURFACE to surface.key),
        )
        gate.request(
            scope = scope,
            chapter = locked,
            onAllowed = controller::continueLockedChapter,
        )
    }

    /*
     * Why: the server refuses a session that ended, so the person signs in again; one still signed in
     * (the refused token was renewed since) only needs to try again.
     */
    fun handleUnavailableAction(
        scope: CoroutineScope,
        surface: ListeningSurface,
    ) {
        val session = controller.state.value.session ?: return
        if (session.status != ListeningStatusModel.NEEDS_SIGN_IN) {
            togglePlayPause(surface)
            return
        }
        scope.launch {
            if (getAuthenticatedUserId() != null) {
                togglePlayPause(surface)
                return@launch
            }
            trackEvent(
                name = AnalyticsEventNames.CHAPTER_LISTENING_SIGN_IN_CLICKED,
                params = mapOf(AnalyticsParams.SURFACE to surface.key),
            )
            navigator.navigate(LoginNavRoute(notifyResultViaSnackbar = true))
        }
    }

    private fun toToggleControl(session: ListeningSessionModel): ListeningControl {
        if (session.isPlaying) return ListeningControl.PAUSE
        return when (session.status) {
            ListeningStatusModel.INTERRUPTED -> ListeningControl.RESUME

            ListeningStatusModel.NEEDS_CONNECTION,
            ListeningStatusModel.NEEDS_SIGN_IN,
            ListeningStatusModel.VERSION_UNAVAILABLE,
            ListeningStatusModel.AUDIO_UNAVAILABLE,
            ListeningStatusModel.NARRATION_PAUSED,
            -> ListeningControl.RETRY

            else -> ListeningControl.PLAY
        }
    }
}
