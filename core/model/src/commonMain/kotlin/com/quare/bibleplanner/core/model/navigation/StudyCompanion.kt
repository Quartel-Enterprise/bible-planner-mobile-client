package com.quare.bibleplanner.core.model.navigation

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.toChapterStudyCompanion
import com.quare.bibleplanner.core.model.route.toDayStudyNavRoute

fun MutableList<NavKey>.syncStudyPanelCompanion(
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

fun List<NavKey>.hasStudyCompanionOnTop(): Boolean {
    val top = lastOrNull()
    val mainPane = getOrNull(lastIndex - 1)
    val isDayStudyCompanion = top is DayStudyNavRoute &&
        mainPane is DayNavRoute &&
        mainPane.toDayStudyNavRoute() == top
    val isChapterStudyCompanion = top is ChapterStudyNavRoute && mainPane is ReadNavRoute
    return isDayStudyCompanion || isChapterStudyCompanion
}
