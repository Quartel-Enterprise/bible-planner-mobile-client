package com.quare.bibleplanner.android.feature.readingplan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.utils.LocalHostedTabBarHeight
import com.quare.bibleplanner.ui.utils.LocalMainFabVisibilityState
import com.quare.bibleplanner.ui.utils.ReserveBottomOverlayHeightEffect

private val fabBottomSpacing = 16.dp

/*
 * Why: the native main screen draws the bar or rail around this tab, like the shared MainTabScaffold does when
 * hosted. The root snackbar sits above the bar and the FABs, and the list keeps clear of the FABs at its end.
 */
@Composable
internal fun ReadingPlanTabScaffold(
    floatingActionButton: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val hostedTabBarHeight = LocalHostedTabBarHeight.current ?: 0.dp
    val navigationBarInsets = WindowInsets.navigationBars
    val density = LocalDensity.current
    val isFabVisible = LocalMainFabVisibilityState.current.isVisible
    var fabAreaHeight by remember { mutableStateOf(0.dp) }
    val reservedFabHeight by animateDpAsState(
        targetValue = if (isFabVisible) fabAreaHeight else 0.dp,
        label = "reading_plan_fab_area_height",
    )
    ReserveBottomOverlayHeightEffect {
        maxOf(
            navigationBarInsets.getBottom(density).toFloat(),
            with(density) { hostedTabBarHeight.toPx() },
        ) + with(density) { reservedFabHeight.toFabClearance().toPx() }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
            ) {
                Box(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        val measuredHeight = with(density) { coordinates.size.height.toDp() }
                        if (measuredHeight > fabAreaHeight) {
                            fabAreaHeight = measuredHeight
                        }
                    },
                ) {
                    floatingActionButton()
                }
            }
        },
    ) { innerPadding ->
        content(innerPadding.withFabClearance(reservedFabHeight))
    }
}

@Composable
private fun PaddingValues.withFabClearance(fabAreaHeight: Dp): PaddingValues {
    if (fabAreaHeight <= 0.dp) return this
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection),
        top = calculateTopPadding(),
        end = calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + fabAreaHeight.toFabClearance(),
    )
}

private fun Dp.toFabClearance(): Dp = if (this > 0.dp) this + fabBottomSpacing else 0.dp
