package com.quare.bibleplanner.core.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.navigationSavedStateConfiguration
import com.quare.bibleplanner.core.navigation.strategy.DayStudyPanelSceneStrategy
import com.quare.bibleplanner.core.navigation.utils.rememberDisplayBackStack
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackDestination
import com.quare.bibleplanner.feature.daystudy.presentation.component.DayStudyBackgroundGenerationOverlay
import com.quare.bibleplanner.feature.inappupdate.presentation.InAppUpdateDownloadOverlay
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject

private val dayStudyPanelMinWidth = 700.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RootAppNavDisplay() {
    val backStack = rememberNavBackStack(navigationSavedStateConfiguration, MainNavRoute)
    val onNavigate: (NavKey) -> Unit = { route ->
        if (backStack.lastOrNull() != route) {
            backStack.add(route)
        }
    }
    val onNavigateBack: () -> Unit = { backStack.removeLastOrNull() }
    val onNavigateReplacingTop: (NavKey) -> Unit = { route ->
        backStack.removeLastOrNull()
        backStack.add(route)
    }
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val appSnackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val appSnackbarController = koinInject<AppSnackbarController>()
    val trackDestination = koinInject<TrackDestination>()
    val currentNavKey = backStack.lastOrNull()
    LaunchedEffect(currentNavKey) {
        currentNavKey?.let(trackDestination::invoke)
    }
    EventBusNavigationListener(onNavigate)
    ActionCollector(appSnackbarController.messages) { message ->
        message.run {
            appSnackbarHostState.showSnackbar(
                message = getString(stringResource),
                withDismissAction = isDismissible,
            )
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > dayStudyPanelMinWidth
        val displayBackStack = rememberDisplayBackStack(isWide = isWide, backStack = backStack)
        CompositionLocalProvider(
            LocalSnackbarHostState provides snackbarHostState,
            LocalIsWideLayout provides isWide,
        ) {
            SharedTransitionLayout {
                NavDisplay(
                    backStack = displayBackStack,
                    onBack = onNavigateBack,
                    sceneStrategies = listOf(
                        DialogSceneStrategy(),
                        remember(isWide) { DayStudyPanelSceneStrategy(isWide) },
                    ),
                    sharedTransitionScope = this@SharedTransitionLayout,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    entryProvider = toEntryProvider(
                        onNavigateBack = onNavigateBack,
                        onNavigateReplacingTop = onNavigateReplacingTop,
                        onNavigate = onNavigate,
                    ),
                )
            }
        }
        DayStudyBackgroundGenerationOverlay()
        SnackbarHost(
            hostState = appSnackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
        InAppUpdateDownloadOverlay(
            onNavigate = onNavigate,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
    }
}

@Composable
private fun EventBusNavigationListener(onNavigate: (NavKey) -> Unit) {
    val navigationEventBus: NavigationEventBus = koinInject()
    ActionCollector(navigationEventBus.events) { route ->
        onNavigate(route)
        navigationEventBus.reset()
    }
}
