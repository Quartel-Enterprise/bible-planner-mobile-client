package com.quare.bibleplanner.core.navigation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.toDayStudyNavRoute

@Composable
internal fun rememberDisplayBackStack(
    isWide: Boolean,
    backStack: MutableList<NavKey>,
): List<NavKey> {
    var wasWide by remember { mutableStateOf(isWide) }
    val isCollapsingCompanion = !isWide && wasWide && backStack.hasDayStudyCompanionOnTop()
    LaunchedEffect(isWide) {
        if (wasWide != isWide) {
            backStack.syncDayStudyPanelCompanion(isWide)
        }
        wasWide = isWide
    }
    return if (isCollapsingCompanion) backStack.dropLast(1) else backStack
}

private fun MutableList<NavKey>.syncDayStudyPanelCompanion(isWide: Boolean) {
    val top = lastOrNull()
    if (isWide) {
        if (top is DayNavRoute) {
            add(top.toDayStudyNavRoute())
        }
    } else if (hasDayStudyCompanionOnTop()) {
        removeLastOrNull()
    }
}

internal fun List<NavKey>.hasDayStudyCompanionOnTop(): Boolean {
    val top = lastOrNull()
    val mainPane = getOrNull(lastIndex - 1)
    return top is DayStudyNavRoute && mainPane is DayNavRoute && mainPane.toDayStudyNavRoute() == top
}
