package com.quare.bibleplanner.core.navigation.utils

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

internal fun NavBackStack<NavKey>.back() {
    if (hasDayStudyCompanionOnTop()) {
        removeLastOrNull()
    }
    removeLastOrNull()
}
