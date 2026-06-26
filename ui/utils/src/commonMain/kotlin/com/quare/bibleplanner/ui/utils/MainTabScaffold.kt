package com.quare.bibleplanner.ui.utils

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
    Row(modifier = Modifier.fillMaxSize()) {
        navigationRail()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = floatingActionButton,
            snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) },
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

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        val measuredHeight = with(density) { coordinates.size.height.toDp() }
                        if (measuredHeight > fabAreaHeight) {
                            fabAreaHeight = measuredHeight
                        }
                    }.graphicsLayer {
                        val maxTranslation =
                            (-scrollBehavior.state.heightOffsetLimit - navigationBarInsets.getBottom(this))
                                .coerceAtLeast(0f)
                        translationY = (-scrollBehavior.state.heightOffset).coerceAtMost(maxTranslation)
                    },
            ) {
                floatingActionButton()
            }
        },
        snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) },
        bottomBar = {
            navigationBar(
                Modifier
                    .graphicsLayer {
                        translationY = -scrollBehavior.state.heightOffset
                    }.onGloballyPositioned { coordinates ->
                        scrollBehavior.state.heightOffsetLimit = -coordinates.size.height.toFloat()
                    },
            )
        },
        content = { paddingValues ->
            CompositionLocalProvider(
                value = LocalMainPadding provides paddingValues.withFabClearance(
                    fabAreaHeight = fabAreaHeight,
                    navigationBarInsets = navigationBarInsets,
                ),
                content = content,
            )
        },
    )
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
        bottom = navigationBarBottom + fabAreaHeight + FAB_BOTTOM_SPACING,
    )
}

private const val WIDE_SCREEN_WIDTH = 600
private val FAB_BOTTOM_SPACING = 16.dp
