package com.quare.bibleplanner.core.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.quare.bibleplanner.core.model.route.ReadingPlanNavRoute
import com.quare.bibleplanner.feature.day.presentation.day
import com.quare.bibleplanner.feature.deleteprogress.presentation.deleteProgress
import com.quare.bibleplanner.feature.editplanstartdate.presentation.editPlanStartDate
import com.quare.bibleplanner.feature.materialyou.presentation.materialYou
import com.quare.bibleplanner.feature.onboardingstartdate.presentation.onboardingStartDate
import com.quare.bibleplanner.feature.readingplan.presentation.readingPlan
import com.quare.bibleplanner.feature.themeselection.presentation.themeSettings

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = ReadingPlanNavRoute,
        ) {
            val sharedTransitionScope = this@SharedTransitionLayout
            readingPlan(
                navController = navController,
                sharedTransitionScope = sharedTransitionScope,
            )
            day(
                navController = navController,
                sharedTransitionScope = sharedTransitionScope,
            )
            themeSettings(navController)
            materialYou(navController)
            deleteProgress(navController)
            editPlanStartDate(navController)
            onboardingStartDate(navController)
        }
    }
}
