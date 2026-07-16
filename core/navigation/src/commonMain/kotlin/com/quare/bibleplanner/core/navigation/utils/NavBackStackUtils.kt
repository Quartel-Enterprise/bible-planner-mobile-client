package com.quare.bibleplanner.core.navigation.utils

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

internal fun NavBackStack<NavKey>.back(): List<NavKey> {
    val removed = mutableListOf<NavKey>()
    if (hasDayStudyCompanionOnTop()) {
        removeLastOrNull()?.let(removed::add)
    }
    removeLastOrNull()?.let(removed::add)
    return removed
}
