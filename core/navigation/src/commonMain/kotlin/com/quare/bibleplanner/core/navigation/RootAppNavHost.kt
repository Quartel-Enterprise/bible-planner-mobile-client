package com.quare.bibleplanner.core.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.addNotesFreeWarning
import com.quare.bibleplanner.feature.bibleversion.presentation.bibleVersionSelectionRoot
import com.quare.bibleplanner.feature.bookdetails.presentation.bookDetails
import com.quare.bibleplanner.feature.congrats.presentation.congrats
import com.quare.bibleplanner.feature.day.presentation.day
import com.quare.bibleplanner.feature.deletenotes.presentation.deleteNotes
import com.quare.bibleplanner.feature.deleteprogress.presentation.deleteProgress
import com.quare.bibleplanner.feature.deleteversion.presentation.deleteVersion
import com.quare.bibleplanner.feature.donation.pixqr.presentation.pixQr
import com.quare.bibleplanner.feature.donation.presentation.donation
import com.quare.bibleplanner.feature.editplanstartdate.presentation.editPlanStartDate
import com.quare.bibleplanner.feature.login.presentation.loginRoot
import com.quare.bibleplanner.feature.main.presentation.mainScreen
import com.quare.bibleplanner.feature.materialyou.presentation.materialYou
import com.quare.bibleplanner.feature.paywall.presentation.paywall
import com.quare.bibleplanner.feature.read.presentation.read
import com.quare.bibleplanner.feature.releasenotes.presentation.releaseNotes
import com.quare.bibleplanner.feature.themeselection.presentation.themeSettings
import com.quare.bibleplanner.ui.utils.MainScaffoldState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RootAppNavHost() {
    val navController = rememberNavController()
    val mainScaffoldState: MainScaffoldState = remember { MainScaffoldState() }
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = MainNavRoute,
        ) {
            val sharedTransitionScope = this@SharedTransitionLayout
            loginRoot(navController)
            mainScreen(mainScaffoldState, navController, sharedTransitionScope)
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
            bibleVersionSelectionRoot(navController)
            deleteVersion(navController)
            read(
                navController = navController,
                sharedTransitionScope = sharedTransitionScope,
            )
        }
    }
}
