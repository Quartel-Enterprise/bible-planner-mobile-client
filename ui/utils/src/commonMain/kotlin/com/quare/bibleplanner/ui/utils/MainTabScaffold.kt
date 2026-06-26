package com.quare.bibleplanner.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
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

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = -scrollBehavior.state.heightOffset
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
                value = LocalMainPadding provides paddingValues,
                content = content,
            )
        },
    )
}

private const val WIDE_SCREEN_WIDTH = 600
