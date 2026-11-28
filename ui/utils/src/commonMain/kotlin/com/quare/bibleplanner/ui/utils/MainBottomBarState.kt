package com.quare.bibleplanner.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class MainBottomBarState {
    private val reservations = mutableStateListOf<BottomOverlayReservation>()

    val visibleHeightPx: Float
        get() = reservations.lastOrNull()?.heightPx ?: 0f

    internal fun register(reservation: BottomOverlayReservation) {
        reservations.add(reservation)
    }

    internal fun release(reservation: BottomOverlayReservation) {
        reservations.remove(reservation)
    }
}

val LocalMainBottomBarState = staticCompositionLocalOf { MainBottomBarState() }

@Composable
fun ReserveBottomOverlayHeightEffect(heightPx: () -> Float) {
    val state = LocalMainBottomBarState.current
    val reservation = remember { BottomOverlayReservation() }
    // Why: callers capture plain values (an animated FAB height, a bar height) the first lambda would freeze.
    val currentHeightPx by rememberUpdatedState(heightPx)
    DisposableEffect(state, reservation) {
        state.register(reservation)
        onDispose { state.release(reservation) }
    }
    LaunchedEffect(reservation) {
        snapshotFlow { currentHeightPx() }.collect { height -> reservation.heightPx = height }
    }
}

/*
 * Why: a tab the native main screen hosts keeps snackbars and the bottom overlays above the visible part of the
 * host's navigation bar (zero beside a rail), which hides on scroll, plus whatever the tab draws over it, such as a
 * FAB.
 */
@Composable
fun ReserveHostedTabBarHeightEffect(extraHeight: Dp) {
    val visibleHostedTabBarHeightPx = rememberVisibleHostedTabBarHeightPx()
    val navigationBarInsets = WindowInsets.navigationBars
    val density = LocalDensity.current
    ReserveBottomOverlayHeightEffect {
        maxOf(
            navigationBarInsets.getBottom(density).toFloat(),
            visibleHostedTabBarHeightPx(),
        ) + with(density) { extraHeight.toPx() }
    }
}

// Why: read inside a layout, draw or snapshotFlow lambda, so the bar sliding away doesn't recompose the caller.
@Composable
internal fun rememberVisibleHostedTabBarHeightPx(): () -> Float {
    val hostedTabBarHeightPx = with(LocalDensity.current) { (LocalHostedTabBarHeight.current ?: 0.dp).toPx() }
    val heightOffset = LocalHostedTabBarHeightOffset.current
    val currentHeightPx by rememberUpdatedState(hostedTabBarHeightPx)
    return remember(heightOffset) { { (currentHeightPx + heightOffset()).coerceAtLeast(0f) } }
}

@Composable
fun mainContentBottomInset(): Dp {
    val state = LocalMainBottomBarState.current
    val density = LocalDensity.current
    val navigationBarBottom = WindowInsets.navigationBars.getBottom(density).toFloat()
    return with(density) { maxOf(navigationBarBottom, state.visibleHeightPx).toDp() }
}
