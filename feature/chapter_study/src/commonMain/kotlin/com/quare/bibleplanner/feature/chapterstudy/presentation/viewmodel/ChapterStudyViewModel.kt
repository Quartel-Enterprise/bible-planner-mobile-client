package com.quare.bibleplanner.feature.chapterstudy.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.chapterstudy.domain.coordinator.ChapterStudyGenerationCoordinator
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyAccessModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationJob
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationStatus
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyPhaseModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.CrossReferenceModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.KeyVerseModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.OutlineSectionModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.PendingVerseFocusModel
import com.quare.bibleplanner.core.chapterstudy.domain.store.PendingVerseFocusStore
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.core.model.route.ChatEntrySource
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.NavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.chapterstudy.domain.usecase.ChapterStudyUseCases
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyContentUiState
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyHeroUiModel
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiEvent
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class ChapterStudyViewModel(
    private val useCases: ChapterStudyUseCases,
    private val generationCoordinator: ChapterStudyGenerationCoordinator,
    private val pendingVerseFocusStore: PendingVerseFocusStore,
    private val studyUnlockResultStore: StudyUnlockResultStore,
    private val navigator: Navigator,
    route: ChapterStudyNavRoute,
    platform: Platform,
    trackEvent: TrackEvent,
) : TrackedViewModel<ChapterStudyUiEvent>(trackEvent) {
    private val target = ChapterStudyTargetModel(
        bookId = BookId.valueOf(route.bookId),
        chapterNumber = route.chapterNumber,
    )
    private val isCompanion: Boolean = route.isCompanion
    private val completionPause: Duration = 700.milliseconds
    private val rewardedUnlockRequestKey: String = listOf(
        REWARDED_UNLOCK_REQUEST_PREFIX,
        target.bookId.name,
        target.chapterNumber.toString(),
    ).joinToString(REQUEST_KEY_SEPARATOR)
    private val targetParams: Map<String, Any> = mapOf(
        AnalyticsParams.BOOK_ID to target.bookId.name,
        AnalyticsParams.CHAPTER_NUMBER to target.chapterNumber,
    )

    /** On a wide window the study opens beside the reader, so the chapter's verses stay in sight. */
    private var isBesideReader: Boolean = false

    val uiState: StateFlow<ChapterStudyUiState>
        field = MutableStateFlow<ChapterStudyUiState>(
            ChapterStudyUiState(
                bookId = target.bookId,
                chapterNumber = target.chapterNumber,
                platform = platform,
                content = ChapterStudyContentUiState.Loading,
            ),
        )

    init {
        viewModelScope.launch { openStudy() }
        observeRewardedUnlock()
    }

    private fun observeRewardedUnlock() {
        viewModelScope.launch {
            studyUnlockResultStore.observeEarned(rewardedUnlockRequestKey).collect {
                startGeneration(isRewarded = true)
            }
        }
    }

    override fun handleEvent(event: ChapterStudyUiEvent) {
        when (event) {
            ChapterStudyUiEvent.OnRetryClick -> onRetryClick()
            ChapterStudyUiEvent.OnGenerateClick -> onGenerateClick()
            is ChapterStudyUiEvent.OnOutlineSectionClick -> onOutlineSectionClick(event.section)
            ChapterStudyUiEvent.OnShareKeyVerseClick -> onShareKeyVerseClick()
            is ChapterStudyUiEvent.OnCrossReferenceClick -> onCrossReferenceClick(event.reference)
            ChapterStudyUiEvent.OnAskAiClick -> onAskAiClick()
            is ChapterStudyUiEvent.OnWidthClassChanged -> isBesideReader = event.isWide
        }
    }

    private suspend fun openStudy() {
        if (!hasGenerationJob()) {
            val cachedStudy = useCases.findCachedStudy(target)
            if (cachedStudy != null) {
                showStudy(
                    study = cachedStudy,
                    isCached = true,
                )
                useCases.refreshCache(target)
                return
            }
            if (isCompanion) observeHero() else startGeneration(isRewarded = false)
        }
        observeGenerationJob()
    }

    /** Logging in or subscribing changes what generating costs, so the offer follows the account. */
    private fun observeHero() {
        combine(
            useCases.observeAuthenticatedUserId(),
            useCases.observeIsProUser(),
        ) { _, isPro -> isPro }
            .distinctUntilChanged()
            .onEach(::showHero)
            .launchIn(viewModelScope)
    }

    private suspend fun showHero(isPro: Boolean) {
        val content = uiState.value.content
        if (content != ChapterStudyContentUiState.Loading && content !is ChapterStudyContentUiState.NotGenerated) return
        val quota = suspendRunCatching { useCases.getQuota(target) }.getOrNull()
        val hero = ChapterStudyHeroUiModel(
            isPro = isPro,
            quota = quota,
            isRewardedUnlockOffered = false,
        )
        showContent(
            ChapterStudyContentUiState.NotGenerated(
                hero = hero.copy(
                    isRewardedUnlockOffered = hero.isLocked &&
                        useCases.prepareRewardedUnlockOffer(quota?.rewardedRemainingToday ?: 0),
                ),
                isStarting = false,
            ),
        )
    }

    private fun onGenerateClick() {
        val notGenerated = uiState.value.content as? ChapterStudyContentUiState.NotGenerated ?: return
        if (notGenerated.isStarting) return
        if (notGenerated.hero.isLocked) {
            onLockedGenerateClick(notGenerated.hero)
            return
        }
        showContent(notGenerated.copy(isStarting = true))
        viewModelScope.launch {
            val access = suspendRunCatching { useCases.getAccess(target) }.getOrDefault(ChapterStudyAccessModel.OPEN)
            when (access) {
                ChapterStudyAccessModel.OPEN -> startGeneration(isRewarded = false)

                ChapterStudyAccessModel.LOGIN_REQUIRED -> {
                    showContent(notGenerated)
                    navigator.navigate(LoginWarningNavRoute(LoginWarningReason.ChapterStudy.key))
                }

                ChapterStudyAccessModel.LIMIT_REACHED -> {
                    showContent(notGenerated)
                    showHero(notGenerated.hero.isPro)
                    navigateToUnlockOrTeaser(replacingTop = false)
                }
            }
        }
    }

    private fun onLockedGenerateClick(hero: ChapterStudyHeroUiModel) {
        if (generationCoordinator.hasUnservedReward(target)) {
            viewModelScope.launch { startGeneration(isRewarded = true) }
        } else {
            navigateToUnlock(
                hero = hero,
                replacingTop = false,
            )
        }
    }

    private fun navigateToUnlockOrTeaser(replacingTop: Boolean) {
        val hero = (uiState.value.content as? ChapterStudyContentUiState.NotGenerated)?.hero
        if (hero == null) {
            navigateTo(
                route = PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_STUDY_LIMIT),
                replacingTop = replacingTop,
            )
            return
        }
        navigateToUnlock(
            hero = hero,
            replacingTop = replacingTop,
        )
    }

    private fun navigateToUnlock(
        hero: ChapterStudyHeroUiModel,
        replacingTop: Boolean,
    ) {
        val route = if (hero.isRewardedUnlockOffered) {
            StudyUnlockNavRoute(
                surface = StudyUnlockSurface.CHAPTER_STUDY,
                paywallSource = PaywallEntrySource.CHAPTER_STUDY,
                requestKey = rewardedUnlockRequestKey,
                rewardedRemainingToday = hero.quota?.rewardedRemainingToday ?: 0,
            )
        } else {
            PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_STUDY_LIMIT)
        }
        navigateTo(
            route = route,
            replacingTop = replacingTop && !hero.isRewardedUnlockOffered,
        )
    }

    private fun navigateTo(
        route: NavRoute,
        replacingTop: Boolean,
    ) {
        if (replacingTop) navigator.navigateReplacingTop(route) else navigator.navigate(route)
    }

    private fun hasGenerationJob(): Boolean = generationCoordinator.jobs.value.any { it.target == target }

    private fun observeGenerationJob() {
        generationCoordinator.jobs
            .map { jobs -> jobs.firstOrNull { it.target == target } }
            .distinctUntilChanged()
            .onEach(::onJobUpdate)
            .launchIn(viewModelScope)
    }

    private suspend fun onJobUpdate(job: ChapterStudyGenerationJob?) {
        when (val status = job?.status) {
            null -> Unit

            ChapterStudyGenerationStatus.Generating -> showContent(
                ChapterStudyContentUiState.Generating(currentPhaseIndex = job.phase?.ordinal ?: 0),
            )

            is ChapterStudyGenerationStatus.Done -> onGenerationDone(status.study)

            is ChapterStudyGenerationStatus.Failed -> onGenerationFailed(status)
        }
    }

    private suspend fun onGenerationDone(study: ChapterStudyModel) {
        if (uiState.value.content is ChapterStudyContentUiState.Generating) {
            showContent(ChapterStudyContentUiState.Generating(currentPhaseIndex = ChapterStudyPhaseModel.entries.size))
            delay(completionPause)
        }
        showStudy(
            study = study,
            isCached = false,
        )
        generationCoordinator.acknowledge(target)
    }

    private fun onGenerationFailed(status: ChapterStudyGenerationStatus.Failed) {
        generationCoordinator.acknowledge(target)
        if (status.isLimitReached && isCompanion) {
            // The reader stays beside it, so the study shows the free ones are used up instead of leaving.
            viewModelScope.launch {
                showContent(ChapterStudyContentUiState.Loading)
                showHero(useCases.observeIsProUser().first())
            }
        } else if (status.isLimitReached) {
            viewModelScope.launch {
                showContent(ChapterStudyContentUiState.Loading)
                showHero(useCases.observeIsProUser().first())
                navigateToUnlockOrTeaser(replacingTop = true)
            }
        } else {
            showContent(ChapterStudyContentUiState.Failed(isOffline = status.isOffline))
        }
    }

    private suspend fun startGeneration(isRewarded: Boolean) {
        val isPro = useCases.observeIsProUser().first()
        if (!useCases.isConnected()) {
            trackEvent(
                name = AnalyticsEventNames.CHAPTER_STUDY_GENERATION_FAILED,
                params = targetParams + mapOf(
                    AnalyticsParams.REASON to OFFLINE_REASON,
                    AnalyticsParams.IS_PRO to isPro,
                ),
            )
            showContent(ChapterStudyContentUiState.Failed(isOffline = true))
            return
        }
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_STUDY_GENERATION_STARTED,
            params = targetParams + mapOf(
                AnalyticsParams.IS_PRO to isPro,
                AnalyticsParams.IS_REWARDED to isRewarded,
            ),
        )
        showContent(ChapterStudyContentUiState.Generating(currentPhaseIndex = 0))
        generationCoordinator.start(
            target = target,
            isRewarded = isRewarded,
        )
    }

    private suspend fun showStudy(
        study: ChapterStudyModel,
        isCached: Boolean,
    ) {
        showContent(
            ChapterStudyContentUiState.Loaded(
                study = study,
                keyVerseText = study.keyVerse?.let { keyVerse -> loadKeyVerseText(keyVerse) },
            ),
        )
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_STUDY_OPENED,
            params = targetParams + mapOf(AnalyticsParams.IS_CACHED to isCached),
        )
    }

    private suspend fun loadKeyVerseText(keyVerse: KeyVerseModel): String? = useCases
        .getVersesShareContent(
            bookId = target.bookId,
            chapterNumber = target.chapterNumber,
            verseNumbers = keyVerse.toVerseNumbers(),
        )?.text

    private fun showContent(content: ChapterStudyContentUiState) {
        uiState.update { it.copy(content = content) }
    }

    private fun onRetryClick() {
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_STUDY_RETRY_CLICKED,
            params = targetParams,
        )
        viewModelScope.launch { startGeneration(isRewarded = generationCoordinator.hasUnservedReward(target)) }
    }

    private fun onOutlineSectionClick(section: OutlineSectionModel) {
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_STUDY_OUTLINE_CLICKED,
            params = targetParams,
        )
        pendingVerseFocusStore.request(
            PendingVerseFocusModel(
                bookId = target.bookId,
                chapterNumber = target.chapterNumber,
                verseNumbers = (section.startVerse..section.endVerse).toList(),
            ),
        )
        if (!isBesideReader) {
            navigator.navigateBack()
        }
    }

    private fun onShareKeyVerseClick() {
        val keyVerse = (uiState.value.content as? ChapterStudyContentUiState.Loaded)?.study?.keyVerse ?: return
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_STUDY_KEY_VERSE_SHARE_CLICKED,
            params = targetParams,
        )
        navigator.navigate(
            ShareVerseNavRoute(
                bookId = target.bookId.name,
                chapterNumber = target.chapterNumber,
                verseNumbers = keyVerse.toVerseNumbers(),
            ),
        )
    }

    private fun onCrossReferenceClick(reference: CrossReferenceModel) {
        viewModelScope.launch {
            navigator.navigate(
                ReadNavRoute(
                    bookId = reference.bookId.name,
                    chapterNumber = reference.chapterNumber,
                    isChapterRead = useCases.isWholeChapterRead(
                        chapterNumber = reference.chapterNumber,
                        bookId = reference.bookId,
                    ),
                    isFromBookDetails = false,
                    targetVerseNumbers = (reference.startVerse..reference.endVerse).toList(),
                ),
            )
        }
    }

    private fun onAskAiClick() {
        trackEvent(
            name = AnalyticsEventNames.AI_CHAT_ENTRY_CLICKED,
            params = mapOf(AnalyticsParams.SOURCE to ChatEntrySource.CHAPTER_STUDY.key),
        )
        navigator.navigate(
            ChatNavRoute(
                source = ChatEntrySource.CHAPTER_STUDY,
                dayNumber = null,
                weekNumber = null,
                readingPlanType = null,
                bookId = target.bookId.name,
                chapterNumber = target.chapterNumber,
            ),
        )
    }

    private fun KeyVerseModel.toVerseNumbers(): List<Int> = (startVerse..endVerse).toList()

    private companion object {
        const val OFFLINE_REASON = "offline"
        const val REWARDED_UNLOCK_REQUEST_PREFIX = "chapter_study"
        const val REQUEST_KEY_SEPARATOR = "|"
    }
}
