package com.quare.bibleplanner.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.quare.bibleplanner.core.model.route.ReadingPlanNavRoute
import com.quare.bibleplanner.feature.day.presentation.day
import com.quare.bibleplanner.feature.deleteprogress.presentation.deleteProgress
import com.quare.bibleplanner.feature.materialyou.presentation.materialYou
import com.quare.bibleplanner.feature.readingplan.presentation.readingPlan
import com.quare.bibleplanner.feature.themeselection.presentation.themeSettings

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ReadingPlanNavRoute,
    ) {
        readingPlan(navController)
        day(navController)
        themeSettings(navController)
        materialYou(navController)
        deleteProgress(navController)
    }
}
