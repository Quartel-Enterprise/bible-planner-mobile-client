package com.quare.bibleplanner.ui.utils.transition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import com.quare.bibleplanner.ui.utils.isIos

@Composable
fun rememberNavigationTransitions(): NavigationTransitions {
    val density = LocalDensity.current
    return remember(density) {
        if (isIos) CupertinoNavigationTransitions else MaterialNavigationTransitions(density)
    }
}
