package com.quare.bibleplanner.core.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isBackPressed
import androidx.compose.ui.input.pointer.isForwardPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationForwardHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.navigation.AppBackStack
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.navigationSavedStateConfiguration
import com.quare.bibleplanner.core.navigation.slot.RootMainTabEntries
import com.quare.bibleplanner.core.navigation.strategy.SheetSceneStrategy
import com.quare.bibleplanner.core.navigation.strategy.StudyPanelSceneStrategy
import com.quare.bibleplanner.core.navigation.strategy.VerseSelectionSceneStrategy
import com.quare.bibleplanner.core.navigation.utils.rememberDisplayBackStack
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackDestination
import com.quare.bibleplanner.feature.bibleversion.presentation.PendingBibleUpdatesPromptOverlay
import com.quare.bibleplanner.feature.daystudy.presentation.component.DayStudyBackgroundGenerationOverlay
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyPanelViewModel
import com.quare.bibleplanner.feature.inappupdate.presentation.InAppUpdateDownloadOverlay
import com.quare.bibleplanner.feature.main.presentation.LocalMainTabEntries
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import com.quare.bibleplanner.ui.utils.bottomPadding
import com.quare.bibleplanner.ui.utils.mainContentBottomInsetPx
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import com.quare.bibleplanner.ui.utils.transition.LocalSharedTransitionScope
import com.quare.bibleplanner.ui.utils.transition.rememberNavigationTransitions
import org.jetbrains.compose.resources.getString
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val dayStudyPanelMinWidth = 700.dp

@Composable
fun RootAppNavDisplay(modifier: Modifier = Modifier) {
    val appBackStack = koinInject<AppBackStack>()
    val savedBackStack = rememberNavBackStack(navigationSavedStateConfiguration, MainNavRoute)
    SavedBackStackEffect(
        appBackStack = appBackStack,
        savedBackStack = savedBackStack,
    )
    val backStack by appBackStack.entries.collectAsState()
    val canNavigateForward by appBackStack.canNavigateForward.collectAsState()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val appSnackbarController = koinInject<AppSnackbarController>()
    val trackDestination = koinInject<TrackDestination>()
    val dayStudyPanelViewModel = koinViewModel<DayStudyPanelViewModel>()
    val dayStudyReadingFraction by dayStudyPanelViewModel.readingFraction.collectAsState()
    val currentNavKey = backStack.lastOrNull()
    LaunchedEffect(currentNavKey) {
        currentNavKey?.let(trackDestination::invoke)
    }
    NavigationForwardHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isForwardEnabled = canNavigateForward,
        onForwardCompleted = appBackStack::navigateForward,
    )
    ActionCollector(appSnackbarController.messages) { message ->
        snackbarHostState.showSnackbar(
            message = when (message) {
                is AppSnackbarMessage.Resource -> getString(message.stringResource)
                is AppSnackbarMessage.Text -> message.text
            },
            withDismissAction = message.isDismissible,
        )
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .mouseBackForwardNavigation(),
    ) {
        val isWide = maxWidth > dayStudyPanelMinWidth
        val displayBackStack = rememberDisplayBackStack(
            isWide = isWide,
            backStack = backStack,
            appBackStack = appBackStack,
        )
        val displayedEntryCount = displayBackStack.size
        /*
         * Why: NavDisplay calls back once per entry the scene leaves, and navigateBack already pops the
         * study with the entry beside it, so the later calls must pop nothing. The size is read from the
         * StateFlow because the collected state only catches up after these synchronous calls.
         */
        val onNavigateBack: () -> Unit = {
            if (appBackStack.entries.value.size >= displayedEntryCount) {
                appBackStack.navigateBack()
            }
        }
        val navigationTransitions = rememberNavigationTransitions()
        val bottomInsetPx = mainContentBottomInsetPx()
        NavigationCommandCollector(appBackStack)
        CompositionLocalProvider(
            LocalSnackbarHostState provides snackbarHostState,
            LocalIsWideLayout provides isWide,
        ) {
            SharedTransitionLayout {
                val koin = getKoin()
                val mainTabEntries = remember(this, koin) {
                    RootMainTabEntries(
                        sharedTransitionScope = this,
                        nativeTabContent = koin.getOrNull<NativeTabContentProvider>(),
                    )
                }
                CompositionLocalProvider(
                    LocalMainTabEntries provides mainTabEntries,
                    LocalSharedTransitionScope provides this,
                ) {
                    NavDisplay(
                        backStack = displayBackStack,
                        onBack = onNavigateBack,
                        sceneStrategies = listOf(
                            SheetSceneStrategy(),
                            DialogSceneStrategy(),
                            remember(isWide, dayStudyReadingFraction) {
                                StudyPanelSceneStrategy(
                                    isWide = isWide,
                                    readingFraction = dayStudyReadingFraction,
                                    onReadingFractionCommit = dayStudyPanelViewModel::onReadingFractionChanged,
                                )
                            },
                            remember(isWide) { VerseSelectionSceneStrategy(isWide = isWide) },
                        ),
                        sharedTransitionScope = this@SharedTransitionLayout,
                        transitionSpec = { navigationTransitions.createForwardTransition(scope = this) },
                        popTransitionSpec = { navigationTransitions.createBackwardTransition(scope = this) },
                        predictivePopTransitionSpec = { swipeEdge ->
                            navigationTransitions.createPredictiveBackTransition(
                                scope = this,
                                swipeEdge = swipeEdge,
                            )
                        },
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                        entryProvider = withNativeEntries(toEntryProvider(mainTabEntries)),
                    )
                }
            }
        }
        DayStudyBackgroundGenerationOverlay()
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .bottomPadding(heightPx = bottomInsetPx),
        )
        InAppUpdateDownloadOverlay(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .bottomPadding(heightPx = bottomInsetPx),
        )
        PendingBibleUpdatesPromptOverlay()
    }
}

@Composable
fun withNativeEntries(sharedEntries: (NavKey) -> NavEntry<NavKey>): (NavKey) -> NavEntry<NavKey> {
    val koin = getKoin()
    val nativeEntries = remember(koin) { koin.getOrNull<NativeEntryProvider>() } ?: return sharedEntries
    return { key -> nativeEntries.entryFor(key) ?: sharedEntries(key) }
}

@Composable
private fun NavigationCommandCollector(appBackStack: AppBackStack) {
    val navigator: Navigator = koinInject()
    LaunchedEffect(appBackStack, navigator) {
        appBackStack.follow(navigator.commands)
    }
}

/*
 * Why: the shared back stack is a process-wide single, so each new composition starts from its own saved
 * routes: a recreated activity gets its screens back, and a fresh one (even in a process that is still
 * alive, such as one kept by the audio service) starts from MainNavRoute.
 */
@Composable
private fun SavedBackStackEffect(
    appBackStack: AppBackStack,
    savedBackStack: MutableList<NavKey>,
) {
    // Why: restored during composition so the first frame already draws the saved screen.
    remember(appBackStack, savedBackStack) {
        appBackStack.restore(savedBackStack.toList())
    }
    LaunchedEffect(appBackStack, savedBackStack) {
        appBackStack.entries.collect { routes ->
            if (savedBackStack.toList() != routes) {
                savedBackStack.clear()
                savedBackStack.addAll(routes)
            }
        }
    }
}

@Composable
private fun Modifier.mouseBackForwardNavigation(): Modifier {
    val dispatcherOwner = LocalNavigationEventDispatcherOwner.current
    val navigationInput = remember { DirectNavigationEventInput() }
    DisposableEffect(dispatcherOwner, navigationInput) {
        val dispatcher = dispatcherOwner?.navigationEventDispatcher
        dispatcher?.addInput(navigationInput)
        onDispose { dispatcher?.removeInput(navigationInput) }
    }
    return pointerInput(dispatcherOwner) {
        if (dispatcherOwner == null) return@pointerInput
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.type == PointerEventType.Press) {
                    if (event.buttons.isBackPressed) {
                        navigationInput.backCompleted()
                    } else if (event.buttons.isForwardPressed) {
                        navigationInput.forwardCompleted()
                    }
                }
            }
        }
    }
}
