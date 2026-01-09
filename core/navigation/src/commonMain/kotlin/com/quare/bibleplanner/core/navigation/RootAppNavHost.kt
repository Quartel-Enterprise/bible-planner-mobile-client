package com.quare.bibleplanner.core.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.addNotesFreeWarning
import com.quare.bibleplanner.feature.congrats.presentation.congrats
import com.quare.bibleplanner.feature.day.presentation.day
import com.quare.bibleplanner.feature.deletenotes.presentation.deleteNotes
import com.quare.bibleplanner.feature.deleteprogress.presentation.deleteProgress
import com.quare.bibleplanner.feature.editplanstartdate.presentation.editPlanStartDate
import com.quare.bibleplanner.feature.main.presentation.mainScreen
import com.quare.bibleplanner.feature.materialyou.presentation.materialYou
import com.quare.bibleplanner.feature.paywall.presentation.paywall
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
            mainScreen(
                mainScaffoldState = mainScaffoldState,
                rootNavController = navController,
                sharedTransitionScope = sharedTransitionScope,
            )
            day(
                navController = navController,
                sharedTransitionScope = sharedTransitionScope,
            )
            themeSettings(navController)
            materialYou(navController)
            deleteProgress(navController)
            deleteNotes(navController)
            addNotesFreeWarning(navController)
            editPlanStartDate(navController)

            paywall(
                navController = navController,
                sharedTransitionScope = sharedTransitionScope,
            )
            congrats(navController)
        }
    }
}
