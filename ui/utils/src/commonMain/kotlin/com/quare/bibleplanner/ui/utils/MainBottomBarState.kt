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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import kotlin.math.roundToInt

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
fun ReserveHostedTabBarHeightEffect(extraHeight: () -> Dp = { 0.dp }) {
    val visibleHostedTabBarClearancePx = rememberVisibleHostedTabBarClearancePx()
    val density = LocalDensity.current
    // Why: a lambda, so an animated FAB height is read by the reservation instead of recomposing the caller.
    ReserveBottomOverlayHeightEffect {
        visibleHostedTabBarClearancePx() + with(density) { extraHeight().toPx() }
    }
}

/*
 * Why: read inside a layout, draw or snapshotFlow lambda, so the bar sliding away doesn't recompose the caller.
 * It is the visible part of the host's bar, and never less than the gesture bar it rests above once hidden.
 */
@Composable
fun rememberVisibleHostedTabBarClearancePx(): () -> Float {
    val hostedTabBarHeightPx = with(LocalDensity.current) { (LocalHostedTabBarHeight.current ?: 0.dp).toPx() }
    val heightOffset = LocalHostedTabBarHeightOffset.current
    val navigationBarInsets = WindowInsets.navigationBars
    val density = LocalDensity.current
    val currentHeightPx by rememberUpdatedState(hostedTabBarHeightPx)
    return remember(
        heightOffset,
        navigationBarInsets,
        density,
    ) {
        {
            getVisibleBarClearancePx(
                barHeightPx = currentHeightPx,
                heightOffsetPx = heightOffset(),
                navigationBarBottomPx = navigationBarInsets.getBottom(density).toFloat(),
            )
        }
    }
}

internal fun getVisibleBarClearancePx(
    barHeightPx: Float,
    heightOffsetPx: Float,
    navigationBarBottomPx: Float,
): Float = maxOf(
    navigationBarBottomPx,
    (barHeightPx + heightOffsetPx).coerceAtLeast(0f),
)

/*
 * Why: the reserved height changes on every frame while a bar hides on scroll, so callers read it in the layout
 * phase through this lambda instead of recomposing on each frame.
 */
@Composable
fun mainContentBottomInsetPx(): () -> Float {
    val state = LocalMainBottomBarState.current
    val navigationBarInsets = WindowInsets.navigationBars
    val density = LocalDensity.current
    return remember(
        state,
        navigationBarInsets,
        density,
    ) {
        { maxOf(navigationBarInsets.getBottom(density).toFloat(), state.visibleHeightPx) }
    }
}

@Composable
fun mainContentBottomInset(): Dp {
    val bottomInsetPx = mainContentBottomInsetPx()
    return with(LocalDensity.current) { bottomInsetPx().toDp() }
}

// Why: pads by a height read in the layout phase, so a height that animates relayouts without recomposing.
fun Modifier.bottomPadding(heightPx: () -> Float): Modifier = layout { measurable, constraints ->
    val padding = heightPx().roundToInt().coerceAtLeast(0)
    val placeable = measurable.measure(constraints.offset(vertical = -padding))
    val height = (placeable.height + padding).coerceIn(
        minimumValue = constraints.minHeight,
        maximumValue = constraints.maxHeight,
    )
    layout(
        width = placeable.width,
        height = height,
    ) {
        placeable.place(
            x = 0,
            y = 0,
        )
    }
}
