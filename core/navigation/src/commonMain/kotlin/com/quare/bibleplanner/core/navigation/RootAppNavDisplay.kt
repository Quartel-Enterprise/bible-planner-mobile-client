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
import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.nav3SavedStateConfiguration
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.addNotesFreeWarning
import com.quare.bibleplanner.feature.applanguage.presentation.appLanguage
import com.quare.bibleplanner.feature.bibleversion.presentation.bibleVersionSelectionRoot
import com.quare.bibleplanner.feature.bookdetails.presentation.bookDetails
import com.quare.bibleplanner.feature.congrats.presentation.congrats
import com.quare.bibleplanner.feature.contactsupport.presentation.contactSupport
import com.quare.bibleplanner.feature.day.presentation.day
import com.quare.bibleplanner.feature.daystudy.presentation.component.DayStudyBackgroundGenerationOverlay
import com.quare.bibleplanner.feature.deletenotes.presentation.deleteNotes
import com.quare.bibleplanner.feature.deleteprogress.presentation.deleteProgress
import com.quare.bibleplanner.feature.deleteversion.presentation.deleteVersion
import com.quare.bibleplanner.feature.donation.pixqr.presentation.pixQr
import com.quare.bibleplanner.feature.donation.presentation.donation
import com.quare.bibleplanner.feature.editplanstartdate.presentation.editPlanStartDate
import com.quare.bibleplanner.feature.login.presentation.loginRoot
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.loginSyncNudge
import com.quare.bibleplanner.feature.loginwarning.presentation.loginWarning
import com.quare.bibleplanner.feature.logout.presentation.logout
import com.quare.bibleplanner.feature.main.presentation.mainScreen
import com.quare.bibleplanner.feature.materialyou.presentation.materialYou
import com.quare.bibleplanner.feature.notificationpermission.presentation.notificationPermission
import com.quare.bibleplanner.feature.paywall.presentation.paywall
import com.quare.bibleplanner.feature.read.presentation.read
import com.quare.bibleplanner.feature.releasenotes.presentation.releaseNotes
import com.quare.bibleplanner.feature.subscriptiondetails.presentation.subscriptionDetails
import com.quare.bibleplanner.feature.themeselection.presentation.themeSettings
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
        if (backStack.lastOrNull() != route) {
            backStack.add(route as NavKey)
        }
    }
    val onNavigateBack: () -> Unit = { backStack.removeLastOrNull() }
    val onNavigateReplacingTop: (Any) -> Unit = { route ->
        backStack.removeLastOrNull()
        backStack.add(route as NavKey)
    }
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
                    onBack = onNavigateBack,
                    sceneStrategies = listOf(DialogSceneStrategy()),
                    sharedTransitionScope = this@SharedTransitionLayout,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    entryProvider = entryProvider {
                        val sharedTransitionScope = this@SharedTransitionLayout
                        loginRoot(onNavigateBack)
                        loginWarning(
                            onNavigateBack = onNavigateBack,
                            onNavigateReplacingTop = onNavigateReplacingTop,
                        )
                        loginSyncNudge(
                            onNavigateBack = onNavigateBack,
                            onNavigateReplacingTop = onNavigateReplacingTop,
                        )
                        logout(
                            onNavigateBack = onNavigateBack,
                            snackbarHostState = snackbarHostState,
                        )
                        mainScreen(
                            onNavigate = onNavigate,
                            sharedTransitionScope = sharedTransitionScope,
                        )
                        day(
                            onNavigate = onNavigate,
                            onNavigateBack = onNavigateBack,
                            sharedTransitionScope = sharedTransitionScope,
                        )
                        themeSettings(
                            onNavigate = onNavigate,
                            onNavigateBack = onNavigateBack,
                        )
                        materialYou(onNavigateBack)
                        deleteProgress(onNavigateBack)
                        deleteNotes(onNavigateBack)
                        addNotesFreeWarning(
                            onNavigateBack = onNavigateBack,
                            onNavigateReplacingTop = onNavigateReplacingTop,
                        )
                        editPlanStartDate(onNavigateBack)
                        releaseNotes(
                            onNavigateBack = onNavigateBack,
                            sharedTransitionScope = sharedTransitionScope,
                        )
                        paywall(
                            onNavigate = onNavigate,
                            onNavigateBack = onNavigateBack,
                            onNavigateReplacingTop = onNavigateReplacingTop,
                            sharedTransitionScope = sharedTransitionScope,
                        )
                        congrats(onNavigateBack)
                        donation(
                            onNavigate = onNavigate,
                            onNavigateBack = onNavigateBack,
                        )
                        pixQr(onNavigateBack)
                        bookDetails(
                            onNavigate = onNavigate,
                            onNavigateBack = onNavigateBack,
                            sharedTransitionScope = sharedTransitionScope,
                        )
                        appLanguage(
                            onNavigate = onNavigate,
                            onNavigateBack = onNavigateBack,
                        )
                        bibleVersionSelectionRoot(
                            onNavigate = onNavigate,
                            onNavigateBack = onNavigateBack,
                        )
                        deleteVersion(onNavigateBack)
                        subscriptionDetails(onNavigateBack)
                        contactSupport(onNavigateBack)
                        read(
                            onNavigate = onNavigate,
                            onNavigateBack = onNavigateBack,
                            onNavigateReplacingTop = onNavigateReplacingTop,
                            sharedTransitionScope = sharedTransitionScope,
                        )
                        notificationPermission(onNavigateBack)
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
