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
    // Saved, because rotating recreates the activity, and a fresh one has to know the window was wide.
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

/**
 * On a wide window the day and the chapter being read each come with their study beside them, and
 * lose it again when the window narrows.
 */
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

/**
 * Whether the top entry is a study shown beside the entry under it: the day's own study, or a
 * chapter study over the reader. On a wide window the pair leaves and is replaced as one.
 */
internal fun List<NavKey>.hasStudyCompanionOnTop(): Boolean {
    val top = lastOrNull()
    val mainPane = getOrNull(lastIndex - 1)
    val isDayStudyCompanion = top is DayStudyNavRoute &&
        mainPane is DayNavRoute &&
        mainPane.toDayStudyNavRoute() == top
    val isChapterStudyCompanion = top is ChapterStudyNavRoute && mainPane is ReadNavRoute
    return isDayStudyCompanion || isChapterStudyCompanion
}
