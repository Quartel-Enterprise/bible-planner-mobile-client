package com.quare.bibleplanner.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val fabBottomSpacing = 16.dp
private const val WIDE_SCREEN_WIDTH = 600
private const val FAB_AREA_HEIGHT_LABEL = "fab_area_height"

@Composable
fun MainTabScaffold(
    navigationBar: @Composable (Modifier) -> Unit,
    navigationRail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        if (maxWidth > WIDE_SCREEN_WIDTH.dp) {
            WideTabScaffold(
                navigationRail = navigationRail,
                floatingActionButton = floatingActionButton,
                content = content,
            )
        } else {
            NarrowTabScaffold(
                navigationBar = navigationBar,
                floatingActionButton = floatingActionButton,
                content = content,
            )
        }
    }
}

@Composable
private fun WideTabScaffold(
    navigationRail: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val navigationBarInsets = WindowInsets.navigationBars
    val density = LocalDensity.current
    var fabAreaHeight by remember { mutableStateOf(0.dp) }
    val reservedFabHeight = rememberReservedFabHeight(fabAreaHeight)
    ReserveBottomOverlayHeight {
        navigationBarInsets.getBottom(density).toFloat() +
            with(density) { reservedFabHeight.toFabClearance().toPx() }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        navigationRail()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                MeasuredFabArea(
                    fabAreaHeight = fabAreaHeight,
                    onFabAreaHeightChange = { fabAreaHeight = it },
                    content = floatingActionButton,
                )
            },
            content = { paddingValues ->
                CompositionLocalProvider(
                    value = LocalMainPadding provides paddingValues,
                    content = content,
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NarrowTabScaffold(
    navigationBar: @Composable (Modifier) -> Unit,
    floatingActionButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    val navigationBarInsets = WindowInsets.navigationBars
    val density = LocalDensity.current
    var fabAreaHeight by remember { mutableStateOf(0.dp) }
    var bottomBarHeightPx by remember { mutableFloatStateOf(0f) }
    val reservedFabHeight = rememberReservedFabHeight(fabAreaHeight)
    ReserveBottomOverlayHeight {
        val visibleBottomBarPx = (bottomBarHeightPx + scrollBehavior.state.heightOffset).coerceAtLeast(0f)
        maxOf(navigationBarInsets.getBottom(density).toFloat(), visibleBottomBarPx) +
            with(density) { reservedFabHeight.toFabClearance().toPx() }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            MeasuredFabArea(
                fabAreaHeight = fabAreaHeight,
                onFabAreaHeightChange = { fabAreaHeight = it },
                modifier = Modifier.graphicsLayer {
                    val maxTranslation =
                        (-scrollBehavior.state.heightOffsetLimit - navigationBarInsets.getBottom(this))
                            .coerceAtLeast(0f)
                    translationY = (-scrollBehavior.state.heightOffset).coerceAtMost(maxTranslation)
                },
                content = floatingActionButton,
            )
        },
        bottomBar = {
            navigationBar(
                Modifier
                    .graphicsLayer {
                        translationY = -scrollBehavior.state.heightOffset
                    }.onGloballyPositioned { coordinates ->
                        bottomBarHeightPx = coordinates.size.height.toFloat()
                        scrollBehavior.state.heightOffsetLimit = -coordinates.size.height.toFloat()
                    },
            )
        },
        content = { paddingValues ->
            CompositionLocalProvider(
                value = LocalMainPadding provides paddingValues.withFabClearance(
                    fabAreaHeight = reservedFabHeight,
                    navigationBarInsets = navigationBarInsets,
                ),
                content = content,
            )
        },
    )
}

@Composable
private fun rememberReservedFabHeight(fabAreaHeight: Dp): Dp {
    val isFabVisible = LocalMainFabVisibilityState.current.isVisible
    val reservedFabHeight by animateDpAsState(
        targetValue = if (isFabVisible) fabAreaHeight else 0.dp,
        label = FAB_AREA_HEIGHT_LABEL,
    )
    return reservedFabHeight
}

@Composable
private fun MeasuredFabArea(
    fabAreaHeight: Dp,
    onFabAreaHeightChange: (Dp) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    AnimatedVisibility(
        visible = LocalMainFabVisibilityState.current.isVisible,
        modifier = modifier,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
    ) {
        Box(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                val measuredHeight = with(density) { coordinates.size.height.toDp() }
                if (measuredHeight > fabAreaHeight) {
                    onFabAreaHeightChange(measuredHeight)
                }
            },
        ) {
            content()
        }
    }
}

@Composable
private fun PaddingValues.withFabClearance(
    fabAreaHeight: Dp,
    navigationBarInsets: WindowInsets,
): PaddingValues {
    if (fabAreaHeight <= 0.dp) return this
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val navigationBarBottom = with(density) { navigationBarInsets.getBottom(this).toDp() }
    return PaddingValues(
        start = calculateStartPadding(layoutDirection),
        top = calculateTopPadding(),
        end = calculateEndPadding(layoutDirection),
        bottom = navigationBarBottom + fabAreaHeight.toFabClearance(),
    )
}

private fun Dp.toFabClearance(): Dp = if (this > 0.dp) this + fabBottomSpacing else 0.dp
