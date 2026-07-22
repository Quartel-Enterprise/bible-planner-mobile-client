package com.quare.bibleplanner.feature.daystudy.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_offline_message
import bibleplanner.feature.day_study.generated.resources.ai_study_wait_for_generations
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.HasCachedStudyUseCase
import com.quare.bibleplanner.feature.daystudy.presentation.factory.DayStudyCardUiModelFactory
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardQuotaUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationPhase
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal class DayStudyViewModel(
    private val getDayStudyQuota: GetDayStudyQuotaUseCase,
    private val hasCachedStudy: HasCachedStudyUseCase,
    private val isConnected: IsConnected,
    private val generationCoordinator: DayStudyGenerationCoordinator,
    private val observeIsProUser: ObserveIsProUser,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val cardUiModelFactory: DayStudyCardUiModelFactory,
    trackEvent: TrackEvent,
) : TrackedViewModel<DayStudyUiEvent>(trackEvent) {
    private val _uiState: MutableStateFlow<DayStudyUiState> = MutableStateFlow(
        DayStudyUiState(
            card = Loadable.Loading,
            generation = null,
            isOpeningStudy = false,
        ),
    )
    val uiState: StateFlow<DayStudyUiState> = _uiState.asStateFlow()

    private val _uiAction: MutableSharedFlow<DayStudyUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<DayStudyUiAction> = _uiAction

    private var passages: List<PassageModel> = emptyList()
    private var dayRoute: DayNavRoute? = null
    private var label: String = ""
    private var jobKey: String? = null
    private var isPro: Boolean = false
    private var observeCardJob: Job? = null
    private var observeJobJob: Job? = null
    private var loadStartMark: TimeMark? = null

    override fun handleEvent(event: DayStudyUiEvent) {
        when (event) {
            is DayStudyUiEvent.OnStart -> onStart(event.passages, event.dayRoute, event.label)
            DayStudyUiEvent.OnCardClick -> onCardClick()
        }
    }

    private fun onStart(
        startPassages: List<PassageModel>,
        route: DayNavRoute,
        studyLabel: String,
    ) {
        val routeChanged = dayRoute != route
        passages = startPassages
        dayRoute = route
        label = studyLabel
        val key = generationCoordinator.keyOf(route)
        jobKey = key
        generationCoordinator.setActive(key)
        if (routeChanged) {
            loadStartMark = TimeSource.Monotonic.markNow()
            _uiState.update { it.copy(card = Loadable.Loading, generation = null, isOpeningStudy = false) }
        }
        observeCard()
        observeJob()
    }

    private fun observeCard() {
        observeCardJob?.cancel()
        observeCardJob = viewModelScope.launch {
            showCachedCard()
            combine(
                observeAuthenticatedUserId(),
                observeIsProUser(),
            ) { userId, pro -> userId to pro }
                .distinctUntilChanged()
                .collectLatest { (_, pro) ->
                    isPro = pro
                    refreshCard(pro)
                }
        }
    }

    private suspend fun showCachedCard() {
        if (_uiState.value.card !is Loadable.Loading) return
        if (!hasCachedStudy(passages)) return
        _uiState.update { state ->
            state.copy(card = Loadable.Loaded(cardUiModelFactory.createFromCache(isPro = isPro)))
        }
        trackLoad(
            pro = isPro,
            isCached = true,
            reason = null,
        )
    }

    private fun observeJob() {
        val key = jobKey ?: return
        observeJobJob?.cancel()
        observeJobJob = viewModelScope.launch {
            generationCoordinator.jobs
                .map { jobs -> jobs.firstOrNull { it.key == key } }
                .distinctUntilChanged()
                .collect(::onJobUpdate)
        }
    }

    private suspend fun onJobUpdate(job: DayStudyGenerationJob?) {
        when (val status = job?.status) {
            null -> _uiState.update { it.copy(generation = null) }

            DayStudyGenerationStatus.Generating -> _uiState.update {
                it.copy(generation = DayStudyGenerationUiModel(job.phase?.toPhaseIndex() ?: 0))
            }

            is DayStudyGenerationStatus.Done -> onJobDone()

            is DayStudyGenerationStatus.Failed -> onJobFailed(status.isLimitReached)
        }
    }

    private suspend fun onJobDone() {
        val key = jobKey ?: return
        refreshCard(isPro)
        _uiState.update { it.copy(generation = null) }
        generationCoordinator.acknowledge(key)
    }

    private suspend fun onJobFailed(isLimitReached: Boolean) {
        val key = jobKey ?: return
        _uiState.update { it.copy(generation = null) }
        if (isLimitReached) lockCard()
        generationCoordinator.acknowledge(key)
    }

    private suspend fun refreshCard(pro: Boolean) {
        suspendRunCatching { getDayStudyQuota(passages) }
            .onSuccess { quota ->
                _uiState.update { state ->
                    state.copy(
                        card = Loadable.Loaded(
                            cardUiModelFactory.create(
                                isPro = pro,
                                quota = quota,
                            ),
                        ),
                    )
                }
                trackLoad(
                    pro = pro,
                    isCached = quota.hasLocalStudy,
                    reason = null,
                )
            }.onFailure { throwable ->
                trackLoad(
                    pro = pro,
                    isCached = false,
                    reason = throwable::class.simpleName ?: UNKNOWN_REASON,
                )
            }
    }

    private fun trackLoad(
        pro: Boolean,
        isCached: Boolean,
        reason: String?,
    ) {
        val mark = loadStartMark ?: return
        loadStartMark = null
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_LOAD,
            params = buildMap {
                put(AnalyticsParams.TARGET, LOAD_TARGET)
                put(AnalyticsParams.DURATION_MS, mark.elapsedNow().inWholeMilliseconds)
                put(AnalyticsParams.SUCCESS, reason == null)
                put(AnalyticsParams.IS_CACHED, isCached)
                put(AnalyticsParams.IS_PRO, pro)
                reason?.let { put(AnalyticsParams.REASON, it) }
            },
        )
    }

    private fun onCardClick() {
        val card = _uiState.value.card.valueOrNull() ?: return
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_CARD_CLICKED,
            params = mapOf(
                AnalyticsParams.CARD_MODE to card.mode.name.lowercase(),
                AnalyticsParams.IS_PRO to card.isPro,
            ),
        )
        if (_uiState.value.generation != null) {
            emitAction(DayStudyUiAction.NavigateToStudy)
            return
        }
        when (card.mode) {
            DayStudyCardMode.LOCKED -> emitAction(DayStudyUiAction.NavigateToPaywall)
            DayStudyCardMode.GENERATE -> generateIfLoggedIn()
            DayStudyCardMode.VIEW -> emitAction(DayStudyUiAction.NavigateToStudy)
        }
    }

    private fun generateIfLoggedIn() {
        viewModelScope.launch {
            withOpeningIndicator {
                if (observeAuthenticatedUserId().first() == null) {
                    _uiAction.emit(DayStudyUiAction.NavigateToLoginWarning)
                } else {
                    startGeneration()
                }
            }
        }
    }

    private suspend fun withOpeningIndicator(block: suspend () -> Unit) {
        _uiState.update { it.copy(isOpeningStudy = true) }
        try {
            block()
        } finally {
            _uiState.update { it.copy(isOpeningStudy = false) }
        }
    }

    private suspend fun startGeneration() {
        val route = dayRoute ?: return
        if (!isConnected()) {
            trackEvent(
                name = AnalyticsEventNames.DAY_STUDY_GENERATION_FAILED,
                params = dayParams(route) + mapOf(
                    AnalyticsParams.REASON to OFFLINE_REASON,
                    AnalyticsParams.IS_PRO to isPro,
                ),
            )
            _uiAction.emit(DayStudyUiAction.ShowSnackBar(Res.string.ai_study_offline_message))
            return
        }
        val quota = getDayStudyQuota(passages)
        if (!canStartFreeGeneration(quota)) return
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_GENERATION_STARTED,
            params = dayParams(route) + mapOf(
                AnalyticsParams.IS_PRO to isPro,
                AnalyticsParams.REMAINING_FREE to quota.remainingFree,
            ),
        )
        jobKey = generationCoordinator.start(passages, route, label)
        _uiState.update { it.copy(generation = DayStudyGenerationUiModel(currentPhaseIndex = 0)) }
        _uiAction.emit(DayStudyUiAction.NavigateToStudy)
    }

    private suspend fun canStartFreeGeneration(quota: DayStudyQuotaModel): Boolean {
        if (isPro || quota.isUnlockedForDay) return true
        val inFlight = generationCoordinator.generatingCount(excludingKey = jobKey)
        if (inFlight < quota.remainingFree) return true
        _uiAction.emit(
            DayStudyUiAction.ShowSnackBarPlural(
                resource = Res.plurals.ai_study_wait_for_generations,
                count = inFlight,
            ),
        )
        return false
    }

    private fun lockCard() {
        _uiState.update { state ->
            val card = state.card.valueOrNull() ?: return@update state
            state.copy(
                card = Loadable.Loaded(
                    card.copy(
                        mode = DayStudyCardMode.LOCKED,
                        quota = Loadable.Loaded(
                            DayStudyCardQuotaUiModel(
                                remainingFree = 0,
                                freeLimit = card.quota.valueOrNull()?.freeLimit ?: 0,
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    private fun dayParams(route: DayNavRoute): Map<String, Any> = mapOf(
        AnalyticsParams.PLAN_TYPE to route.readingPlanType,
        AnalyticsParams.WEEK_NUMBER to route.weekNumber,
        AnalyticsParams.DAY_NUMBER to route.dayNumber,
    )

    private fun emitAction(action: DayStudyUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }

    override fun onCleared() {
        super.onCleared()
        jobKey?.let(generationCoordinator::clearActive)
    }

    private companion object {
        const val OFFLINE_REASON = "offline"
        const val UNKNOWN_REASON = "unknown"
        const val LOAD_TARGET = "card"
    }
}

private fun DayStudyPhaseModel.toPhaseIndex(): Int = when (this) {
    DayStudyPhaseModel.READING -> DayStudyGenerationPhase.READING.ordinal
    DayStudyPhaseModel.CHAPTERS -> DayStudyGenerationPhase.CHAPTERS.ordinal
    DayStudyPhaseModel.CONTEXT -> DayStudyGenerationPhase.CONTEXT.ordinal
    DayStudyPhaseModel.QUESTIONS -> DayStudyGenerationPhase.QUESTIONS.ordinal
}
