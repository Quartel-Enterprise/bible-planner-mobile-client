package com.quare.bibleplanner.feature.studyunlock.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdAvailability
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdFailureReason
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdResult
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdService
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.feature.studyunlock.presentation.model.StudyUnlockUiEvent
import com.quare.bibleplanner.feature.studyunlock.presentation.model.StudyUnlockUiState
import com.quare.bibleplanner.feature.studyunlock.presentation.model.StudyUnlockVideoState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class StudyUnlockViewModel(
    private val route: StudyUnlockNavRoute,
    private val navigator: Navigator,
    private val rewardedAdService: RewardedAdService,
    private val resultStore: StudyUnlockResultStore,
    trackEvent: TrackEvent,
) : TrackedViewModel<StudyUnlockUiEvent>(trackEvent) {
    val uiState: StateFlow<StudyUnlockUiState>
        field = MutableStateFlow(
            StudyUnlockUiState(
                surface = route.surface,
                rewardedRemainingToday = route.rewardedRemainingToday,
                videoState = StudyUnlockVideoState.LOADING,
            ),
        )

    private val surfaceParams: Map<String, Any> = mapOf(AnalyticsParams.SURFACE to route.surface.key)

    init {
        trackEvent(
            name = AnalyticsEventNames.UNLOCK_SHEET_VIEWED,
            params = surfaceParams + mapOf(AnalyticsParams.REWARDED_REMAINING_TODAY to route.rewardedRemainingToday),
        )
        rewardedAdService.preload()
        observeAvailability()
    }

    override fun handleEvent(event: StudyUnlockUiEvent) {
        when (event) {
            StudyUnlockUiEvent.OnSubscribeClick -> onSubscribeClick()
            StudyUnlockUiEvent.OnWatchVideoClick -> onWatchVideoClick()
            StudyUnlockUiEvent.OnDismiss -> navigator.navigateBack()
        }
    }

    private fun observeAvailability() {
        viewModelScope.launch {
            rewardedAdService.availability.collect { availability ->
                if (uiState.value.videoState == StudyUnlockVideoState.PLAYING) return@collect
                uiState.update { it.copy(videoState = availability.toVideoState()) }
                availability.toLoadFailureReason()?.let(::trackFailure)
            }
        }
    }

    private fun onSubscribeClick() {
        trackEvent(
            name = AnalyticsEventNames.UNLOCK_SUBSCRIBE_CLICKED,
            params = surfaceParams,
        )
        navigator.navigateReplacingTop(PaywallNavRoute(route.paywallSource))
    }

    private fun onWatchVideoClick() {
        if (uiState.value.videoState != StudyUnlockVideoState.READY) return
        trackEvent(
            name = AnalyticsEventNames.REWARDED_AD_STARTED,
            params = surfaceParams,
        )
        uiState.update { it.copy(videoState = StudyUnlockVideoState.PLAYING) }
        viewModelScope.launch {
            when (val result = rewardedAdService.show()) {
                RewardedAdResult.Earned -> onRewardEarned()
                RewardedAdResult.Dismissed -> onVideoDismissed()
                is RewardedAdResult.Failed -> onVideoFailed(result.reason)
            }
        }
    }

    private fun onRewardEarned() {
        trackEvent(
            name = AnalyticsEventNames.REWARDED_AD_EARNED,
            params = surfaceParams,
        )
        navigator.navigateBack()
        resultStore.publishEarned(route.requestKey)
    }

    private fun onVideoDismissed() {
        trackEvent(
            name = AnalyticsEventNames.REWARDED_AD_DISMISSED,
            params = surfaceParams,
        )
        reloadVideo()
    }

    private fun onVideoFailed(reason: RewardedAdFailureReason) {
        trackFailure(reason)
        reloadVideo()
    }

    private fun reloadVideo() {
        uiState.update { it.copy(videoState = rewardedAdService.availability.value.toVideoState()) }
        rewardedAdService.preload()
    }

    private fun trackFailure(reason: RewardedAdFailureReason) {
        trackEvent(
            name = AnalyticsEventNames.REWARDED_AD_FAILED,
            params = surfaceParams + mapOf(AnalyticsParams.REASON to reason.name.lowercase()),
        )
    }

    private fun RewardedAdAvailability.toVideoState(): StudyUnlockVideoState = when (this) {
        RewardedAdAvailability.IDLE, RewardedAdAvailability.LOADING -> StudyUnlockVideoState.LOADING
        RewardedAdAvailability.READY -> StudyUnlockVideoState.READY
        RewardedAdAvailability.NO_FILL, RewardedAdAvailability.LOAD_ERROR -> StudyUnlockVideoState.UNAVAILABLE
    }

    private fun RewardedAdAvailability.toLoadFailureReason(): RewardedAdFailureReason? = when (this) {
        RewardedAdAvailability.NO_FILL -> RewardedAdFailureReason.NO_FILL
        RewardedAdAvailability.LOAD_ERROR -> RewardedAdFailureReason.LOAD_ERROR
        RewardedAdAvailability.IDLE, RewardedAdAvailability.LOADING, RewardedAdAvailability.READY -> null
    }
}
