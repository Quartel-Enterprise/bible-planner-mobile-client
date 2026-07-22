package com.quare.bibleplanner.feature.daystudy.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_error
import bibleplanner.feature.day_study.generated.resources.ai_study_limit_reached_message
import bibleplanner.feature.day_study.generated.resources.ai_study_offline_message
import bibleplanner.feature.day_study.generated.resources.ai_study_wait_for_generations
import com.quare.bibleplanner.core.books.util.getReadingLabel
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.toDayNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayPassagesForDayStudyUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.HasCachedStudyUseCase
import com.quare.bibleplanner.feature.daystudy.presentation.factory.DayStudyCardUiModelFactory
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardQuotaUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationPhase
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal class DayStudyRouteViewModel(
    route: DayStudyNavRoute,
    private val getDayPassages: GetDayPassagesForDayStudyUseCase,
    private val getDayStudy: GetDayStudyUseCase,
    private val getDayStudyQuota: GetDayStudyQuotaUseCase,
    private val hasCachedStudy: HasCachedStudyUseCase,
    private val isConnected: IsConnected,
    private val generationCoordinator: DayStudyGenerationCoordinator,
    private val observeIsProUser: ObserveIsProUser,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val cardUiModelFactory: DayStudyCardUiModelFactory,
    platform: Platform,
    trackEvent: TrackEvent,
) : TrackedViewModel<DayStudyRouteUiEvent>(trackEvent) {
    private val _uiState: MutableStateFlow<DayStudyRouteUiState> = MutableStateFlow(
        DayStudyRouteUiState(
            card = Loadable.Loading,
            generation = null,
            openStudy = null,
            isOpeningStudy = false,
            passageLabel = null,
            platform = platform,
        ),
    )
    val uiState: StateFlow<DayStudyRouteUiState> = _uiState.asStateFlow()

    private val _uiAction: MutableSharedFlow<DayStudyRouteUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<DayStudyRouteUiAction> = _uiAction

    private val completionPause = 700.milliseconds

    private val dayRoute: DayNavRoute = route.toDayNavRoute()
    private val jobKey: String = generationCoordinator.keyOf(dayRoute)
    private var passages: List<PassageModel> = emptyList()
    private var label: String = ""
    private var isPro: Boolean = false
    private var isStarted = false
    private var loadStartMark: TimeMark? = TimeSource.Monotonic.markNow()

    init {
        generationCoordinator.setActive(jobKey)
        observePassages()
    }

    override fun handleEvent(event: DayStudyRouteUiEvent) {
        when (event) {
            DayStudyRouteUiEvent.OnCardClick -> onCardClick()
        }
    }

    private fun observePassages() {
        getDayPassages(
            weekNumber = dayRoute.weekNumber,
            dayNumber = dayRoute.dayNumber,
            readingPlanType = ReadingPlanType.valueOf(dayRoute.readingPlanType),
        ).filterNotNull()
            .onEach(::onPassagesLoaded)
            .launchIn(viewModelScope)
    }

    private suspend fun onPassagesLoaded(loaded: List<PassageModel>) {
        passages = loaded
        label = loaded.getReadingLabel()
        _uiState.update { it.copy(passageLabel = label) }
        if (isStarted) return
        isStarted = true
        observeCard()
        observeJob()
    }

    private fun observeCard() {
        viewModelScope.launch {
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
        generationCoordinator.jobs
            .map { jobs -> jobs.firstOrNull { it.key == jobKey } }
            .distinctUntilChanged()
            .onEach(::onJobUpdate)
            .launchIn(viewModelScope)
    }

    private suspend fun onJobUpdate(job: DayStudyGenerationJob?) {
        when (val status = job?.status) {
            null -> Unit

            DayStudyGenerationStatus.Generating -> _uiState.update {
                it.copy(generation = DayStudyGenerationUiModel(job.phase?.toPhaseIndex() ?: 0))
            }

            is DayStudyGenerationStatus.Done -> onJobDone(status.study)

            is DayStudyGenerationStatus.Failed -> onJobFailed(status.isLimitReached)
        }
    }

    private suspend fun onJobDone(study: DayStudyModel) {
        if (_uiState.value.generation != null) completeGenerationPhases()
        _uiState.update { it.copy(generation = null, openStudy = study) }
        trackStudyOpened(isCached = false)
        refreshCard(isPro)
        generationCoordinator.acknowledge(jobKey)
    }

    private suspend fun onJobFailed(isLimitReached: Boolean) {
        _uiState.update { it.copy(generation = null) }
        if (isLimitReached) lockCard()
        _uiAction.emit(
            DayStudyRouteUiAction.ShowSnackBar(
                if (isLimitReached) Res.string.ai_study_limit_reached_message else Res.string.ai_study_error,
            ),
        )
        generationCoordinator.acknowledge(jobKey)
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
        if (_uiState.value.openStudy != null || _uiState.value.generation != null) return
        when (card.mode) {
            DayStudyCardMode.LOCKED -> emitAction(DayStudyRouteUiAction.NavigateToRoute(PaywallNavRoute))
            DayStudyCardMode.GENERATE -> generateIfLoggedIn()
            DayStudyCardMode.VIEW -> generateOrOpen()
        }
    }

    private fun generateIfLoggedIn() {
        viewModelScope.launch {
            withOpeningIndicator {
                if (observeAuthenticatedUserId().first() == null) {
                    _uiAction.emit(
                        DayStudyRouteUiAction.NavigateToRoute(
                            LoginWarningNavRoute(LoginWarningReason.DayStudy.key),
                        ),
                    )
                } else {
                    startGenerationOrCachedOpen()
                }
            }
        }
    }

    private fun generateOrOpen() {
        viewModelScope.launch { withOpeningIndicator { startGenerationOrCachedOpen() } }
    }

    private suspend fun withOpeningIndicator(block: suspend () -> Unit) {
        _uiState.update { it.copy(isOpeningStudy = true) }
        try {
            block()
        } finally {
            _uiState.update { it.copy(isOpeningStudy = false) }
        }
    }

    private suspend fun startGenerationOrCachedOpen() {
        if (hasCachedStudy(passages)) {
            openCachedStudy()
            return
        }
        if (!isConnected()) {
            trackEvent(
                name = AnalyticsEventNames.DAY_STUDY_GENERATION_FAILED,
                params = dayParams() + mapOf(
                    AnalyticsParams.REASON to OFFLINE_REASON,
                    AnalyticsParams.IS_PRO to isPro,
                ),
            )
            _uiAction.emit(DayStudyRouteUiAction.ShowSnackBar(Res.string.ai_study_offline_message))
            return
        }
        val quota = getDayStudyQuota(passages)
        if (!canStartFreeGeneration(quota)) return
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_GENERATION_STARTED,
            params = dayParams() + mapOf(
                AnalyticsParams.IS_PRO to isPro,
                AnalyticsParams.REMAINING_FREE to quota.remainingFree,
            ),
        )
        generationCoordinator.start(passages, dayRoute, label)
        _uiState.update { it.copy(generation = DayStudyGenerationUiModel(currentPhaseIndex = 0)) }
    }

    private suspend fun canStartFreeGeneration(quota: DayStudyQuotaModel): Boolean {
        if (isPro || quota.isUnlockedForDay) return true
        val inFlight = generationCoordinator.generatingCount(excludingKey = jobKey)
        if (inFlight < quota.remainingFree) return true
        _uiAction.emit(
            DayStudyRouteUiAction.ShowSnackBarPlural(
                resource = Res.plurals.ai_study_wait_for_generations,
                count = inFlight,
            ),
        )
        return false
    }

    private suspend fun openCachedStudy() {
        val study = getDayStudy(passages)
            .mapNotNull { (it as? DayStudyGenerationEventModel.Completed)?.study }
            .first()
        _uiState.update { it.copy(openStudy = study) }
        trackStudyOpened(isCached = true)
    }

    private suspend fun completeGenerationPhases() {
        _uiState.update { state ->
            state.copy(
                generation = state.generation?.copy(
                    currentPhaseIndex = DayStudyGenerationPhase.entries.size,
                ),
            )
        }
        delay(completionPause)
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

    private fun trackStudyOpened(isCached: Boolean) {
        trackEvent(
            name = AnalyticsEventNames.DAY_STUDY_OPENED,
            params = mapOf(AnalyticsParams.IS_CACHED to isCached),
        )
    }

    private fun dayParams(): Map<String, Any> = mapOf(
        AnalyticsParams.PLAN_TYPE to dayRoute.readingPlanType,
        AnalyticsParams.WEEK_NUMBER to dayRoute.weekNumber,
        AnalyticsParams.DAY_NUMBER to dayRoute.dayNumber,
    )

    private fun emitAction(action: DayStudyRouteUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }

    override fun onCleared() {
        super.onCleared()
        generationCoordinator.clearActive(jobKey)
    }

    private companion object {
        const val OFFLINE_REASON = "offline"
        const val UNKNOWN_REASON = "unknown"
        const val LOAD_TARGET = "panel"
    }
}

private fun DayStudyPhaseModel.toPhaseIndex(): Int = when (this) {
    DayStudyPhaseModel.READING -> DayStudyGenerationPhase.READING.ordinal
    DayStudyPhaseModel.CHAPTERS -> DayStudyGenerationPhase.CHAPTERS.ordinal
    DayStudyPhaseModel.CONTEXT -> DayStudyGenerationPhase.CONTEXT.ordinal
    DayStudyPhaseModel.QUESTIONS -> DayStudyGenerationPhase.QUESTIONS.ordinal
}
