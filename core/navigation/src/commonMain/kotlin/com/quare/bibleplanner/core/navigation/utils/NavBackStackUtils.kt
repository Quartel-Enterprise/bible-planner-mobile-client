package com.quare.bibleplanner.core.navigation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.navigation.AppBackStack
import com.quare.bibleplanner.core.model.navigation.hasStudyCompanionOnTop

@Composable
internal fun rememberDisplayBackStack(
    isWide: Boolean,
    appBackStack: AppBackStack,
): List<NavKey> {
    val backStack by appBackStack.entries.collectAsState()
    // Why: saved because rotation recreates the activity and the new one must know it was wide.
    var wasWide by rememberSaveable { mutableStateOf(isWide) }
    val isCollapsingCompanion = !isWide && wasWide && backStack.hasStudyCompanionOnTop()
    val topRoute = backStack.lastOrNull()
    LaunchedEffect(isWide, topRoute) {
        appBackStack.updateLayout(
            isWide = isWide,
            isCollapsingCompanion = isCollapsingCompanion,
        )
        wasWide = isWide
    }
    return if (isCollapsingCompanion) backStack.dropLast(1) else backStack
}
