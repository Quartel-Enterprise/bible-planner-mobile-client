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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.addNotesFreeWarning
import com.quare.bibleplanner.feature.applanguage.presentation.appLanguage
import com.quare.bibleplanner.feature.bibleversion.presentation.bibleVersionSelectionRoot
import com.quare.bibleplanner.feature.bookdetails.presentation.bookDetails
import com.quare.bibleplanner.feature.congrats.presentation.congrats
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
import com.quare.bibleplanner.feature.themeselection.presentation.themeSettings
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RootAppNavHost() {
    val navController = rememberNavController()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    // Dedicated host for app-wide messages (e.g. login result). It lives at the root as an
    // always-present overlay so a global snackbar is shown regardless of which screen is on
    // top — unlike per-screen hosts, whose Scaffold may not be composed when the message is
    // pushed (e.g. while the login dialog is open over the day screen).
    val appSnackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val appSnackbarController = koinInject<AppSnackbarController>()
    EventBusNavigationListener(navController)
    ActionCollector(appSnackbarController.messages) { message ->
        appSnackbarHostState.showSnackbar(getString(message))
    }
    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = MainNavRoute,
                ) {
                    val sharedTransitionScope = this@SharedTransitionLayout
                    loginRoot(navController)
                    loginWarning(navController)
                    loginSyncNudge(navController)
                    logout(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                    )
                    mainScreen(navController, sharedTransitionScope)
                    day(navController, sharedTransitionScope)
                    themeSettings(navController)
                    materialYou(navController)
                    deleteProgress(navController)
                    deleteNotes(navController)
                    addNotesFreeWarning(navController)
                    editPlanStartDate(navController)
                    releaseNotes(navController, sharedTransitionScope)
                    paywall(navController, sharedTransitionScope)
                    congrats(navController)
                    donation(navController)
                    pixQr(navController)
                    bookDetails(navController, sharedTransitionScope)
                    appLanguage(navController)
                    bibleVersionSelectionRoot(navController)
                    deleteVersion(navController)
                    read(
                        navController = navController,
                        sharedTransitionScope = sharedTransitionScope,
                    )
                    notificationPermission(navController)
                }
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
private fun EventBusNavigationListener(navController: NavHostController) {
    val navigationEventBus: NavigationEventBus = koinInject()
    ActionCollector(navigationEventBus.events) { route ->
        navController.navigate(route) { launchSingleTop = true }
        navigationEventBus.reset()
    }
}
