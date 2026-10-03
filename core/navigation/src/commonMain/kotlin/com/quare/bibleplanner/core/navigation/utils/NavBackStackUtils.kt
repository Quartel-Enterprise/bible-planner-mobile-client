package com.quare.bibleplanner.core.navigation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.toChapterStudyCompanion
import com.quare.bibleplanner.core.model.route.toDayStudyNavRoute

@Composable
internal fun rememberDisplayBackStack(
    isWide: Boolean,
    backStack: MutableList<NavKey>,
): List<NavKey> {
    // Why: saved because rotation recreates the activity and the new one must know it was wide.
    var wasWide by rememberSaveable { mutableStateOf(isWide) }
    val isCollapsingCompanion = !isWide && wasWide && backStack.hasStudyCompanionOnTop()
    val topRoute = backStack.lastOrNull()
    LaunchedEffect(isWide, topRoute) {
        backStack.syncStudyPanelCompanion(
            isWide = isWide,
            isCollapsingCompanion = isCollapsingCompanion,
        )
        wasWide = isWide
    }
    return if (isCollapsingCompanion) backStack.dropLast(1) else backStack
}

private fun MutableList<NavKey>.syncStudyPanelCompanion(
    isWide: Boolean,
    isCollapsingCompanion: Boolean,
) {
    val top = lastOrNull()
    if (isWide) {
        when (top) {
            is DayNavRoute -> add(top.toDayStudyNavRoute())
            is ReadNavRoute -> add(top.toChapterStudyCompanion())
        }
    } else if (isCollapsingCompanion) {
        removeLastOrNull()
    }
}

internal fun List<NavKey>.hasStudyCompanionOnTop(): Boolean {
    val top = lastOrNull()
    val mainPane = getOrNull(lastIndex - 1)
    val isDayStudyCompanion = top is DayStudyNavRoute &&
        mainPane is DayNavRoute &&
        mainPane.toDayStudyNavRoute() == top
    val isChapterStudyCompanion = top is ChapterStudyNavRoute && mainPane is ReadNavRoute
    return isDayStudyCompanion || isChapterStudyCompanion
}
