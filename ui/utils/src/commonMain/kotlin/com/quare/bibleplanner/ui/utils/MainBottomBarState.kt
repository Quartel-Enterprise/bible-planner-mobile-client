package com.quare.bibleplanner.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

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
fun ReserveBottomOverlayHeight(heightPx: () -> Float) {
    val state = LocalMainBottomBarState.current
    val reservation = remember { BottomOverlayReservation() }
    DisposableEffect(state, reservation) {
        state.register(reservation)
        onDispose { state.release(reservation) }
    }
    LaunchedEffect(reservation) {
        snapshotFlow(heightPx).collect { height -> reservation.heightPx = height }
    }
}

@Composable
fun mainContentBottomInset(): Dp {
    val state = LocalMainBottomBarState.current
    val density = LocalDensity.current
    val navigationBarBottom = WindowInsets.navigationBars.getBottom(density).toFloat()
    return with(density) { maxOf(navigationBarBottom, state.visibleHeightPx).toDp() }
}
