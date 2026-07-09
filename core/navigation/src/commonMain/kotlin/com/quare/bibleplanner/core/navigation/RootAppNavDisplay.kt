package com.quare.bibleplanner.core.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.nav3SavedStateConfiguration
import com.quare.bibleplanner.feature.day.presentation.day
import com.quare.bibleplanner.feature.daystudy.presentation.component.DayStudyBackgroundGenerationOverlay
import com.quare.bibleplanner.feature.logout.presentation.logout
import com.quare.bibleplanner.feature.main.presentation.mainScreen
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RootAppNavDisplay() {
    val backStack = rememberNavBackStack(nav3SavedStateConfiguration, MainNavRoute)
    val onNavigate: (Any) -> Unit = { route ->
        if (route is NavKey) {
            if (backStack.lastOrNull() != route) {
                backStack.add(route)
            }
        } else {
            Logger.w { "Route not migrated to Navigation 3 yet: $route" }
        }
    }
    val onNavigateBack: () -> Unit = { backStack.removeLastOrNull() }
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val appSnackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val appSnackbarController = koinInject<AppSnackbarController>()
    EventBusNavigationListener(onNavigate)
    ActionCollector(appSnackbarController.messages) { message ->
        appSnackbarHostState.showSnackbar(getString(message))
    }
    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
            SharedTransitionLayout {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    sceneStrategies = listOf(DialogSceneStrategy()),
                    sharedTransitionScope = this@SharedTransitionLayout,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    entryProvider = entryProvider {
                        mainScreen(
                            onNavigate = onNavigate,
                            sharedTransitionScope = this@SharedTransitionLayout,
                        )
                        day(
                            onNavigate = onNavigate,
                            onNavigateBack = onNavigateBack,
                            sharedTransitionScope = this@SharedTransitionLayout,
                        )
                        logout(
                            onNavigateBack = onNavigateBack,
                            snackbarHostState = snackbarHostState,
                        )
                    },
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
    }
}

@Composable
private fun EventBusNavigationListener(onNavigate: (Any) -> Unit) {
    val navigationEventBus: NavigationEventBus = koinInject()
    ActionCollector(navigationEventBus.events) { route ->
        onNavigate(route)
        navigationEventBus.reset()
    }
}
