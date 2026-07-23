package com.quare.bibleplanner.core.navigation.utils

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

internal fun NavBackStack<NavKey>.back(isWide: Boolean): List<NavKey> {
    val removed = mutableListOf<NavKey>()
    if (isWide && hasDayStudyCompanionOnTop() && canPopEntry) {
        removeLastOrNull()?.let(removed::add)
    }
    if (canPopEntry) {
        removeLastOrNull()?.let(removed::add)
    }
    return removed
}

private val List<NavKey>.canPopEntry: Boolean
    get() = size > 1
