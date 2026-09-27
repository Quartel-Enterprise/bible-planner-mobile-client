package com.quare.bibleplanner.ui.utils.transition

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent

class MaterialNavigationTransitions(
    density: Density,
) : NavigationTransitions {
    private val slideDistancePx = with(density) { 30.dp.roundToPx() }
    private val predictiveBackMarginPx = with(density) { 8.dp.roundToPx() }
    private val standardDecelerateEasing = CubicBezierEasing(0f, 0f, 0f, 1f)

    override fun createForwardTransition(scope: AnimatedContentTransitionScope<Scene<NavKey>>): ContentTransform =
        createSharedAxisXTransition(enterSide = RIGHT)

    override fun createBackwardTransition(scope: AnimatedContentTransitionScope<Scene<NavKey>>): ContentTransform =
        createSharedAxisXTransition(enterSide = LEFT)

    override fun createPredictiveBackTransition(
        scope: AnimatedContentTransitionScope<Scene<NavKey>>,
        swipeEdge: Int,
    ): ContentTransform {
        val shiftSide = if (swipeEdge == NavigationEvent.EDGE_LEFT) RIGHT else LEFT
        val gestureSpec = tween<Float>(
            durationMillis = DURATION_MILLIS,
            easing = standardDecelerateEasing,
        )
        val enter = fadeIn(
            animationSpec = tween(
                durationMillis = INCOMING_DURATION_MILLIS,
                delayMillis = OUTGOING_DURATION_MILLIS,
                easing = LinearEasing,
            ),
        ) + scaleIn(
            animationSpec = gestureSpec,
            initialScale = PREDICTIVE_BACK_ENTER_SCALE,
        )
        val exit = fadeOut(
            animationSpec = tween(
                durationMillis = OUTGOING_DURATION_MILLIS,
                easing = LinearEasing,
            ),
        ) + scaleOut(
            animationSpec = gestureSpec,
            targetScale = PREDICTIVE_BACK_EXIT_SCALE,
        ) + slideOutHorizontally(
            animationSpec = tween(
                durationMillis = DURATION_MILLIS,
                easing = standardDecelerateEasing,
            ),
            targetOffsetX = { fullWidth ->
                shiftSide * (fullWidth / PREDICTIVE_BACK_SHIFT_DIVISOR - predictiveBackMarginPx).coerceAtLeast(0)
            },
        )
        return enter togetherWith exit
    }

    override fun createTabSwitchTransition(scope: AnimatedContentTransitionScope<Scene<NavKey>>): ContentTransform =
        fadeIn(animationSpec = tween(TAB_SWITCH_DURATION_MILLIS)) togetherWith
            fadeOut(animationSpec = tween(TAB_SWITCH_DURATION_MILLIS))

    private fun createSharedAxisXTransition(enterSide: Int): ContentTransform {
        val slideSpec = tween<IntOffset>(
            durationMillis = DURATION_MILLIS,
            easing = FastOutSlowInEasing,
        )
        val enter = slideInHorizontally(
            animationSpec = slideSpec,
            initialOffsetX = { enterSide * slideDistancePx },
        ) + fadeIn(animationSpec = createIncomingSpec())
        val exit = slideOutHorizontally(
            animationSpec = slideSpec,
            targetOffsetX = { -enterSide * slideDistancePx },
        ) + fadeOut(animationSpec = createOutgoingSpec())
        return enter togetherWith exit
    }

    private fun createIncomingSpec() = tween<Float>(
        durationMillis = INCOMING_DURATION_MILLIS,
        delayMillis = OUTGOING_DURATION_MILLIS,
        easing = LinearOutSlowInEasing,
    )

    private fun createOutgoingSpec() = tween<Float>(
        durationMillis = OUTGOING_DURATION_MILLIS,
        easing = FastOutLinearInEasing,
    )

    companion object {
        private const val DURATION_MILLIS = 300
        private const val OUTGOING_DURATION_MILLIS = 105
        private const val INCOMING_DURATION_MILLIS = DURATION_MILLIS - OUTGOING_DURATION_MILLIS
        private const val RIGHT = 1
        private const val LEFT = -1
        private const val TAB_SWITCH_DURATION_MILLIS = 150
        private const val PREDICTIVE_BACK_ENTER_SCALE = 1.1f
        private const val PREDICTIVE_BACK_EXIT_SCALE = 0.9f
        private const val PREDICTIVE_BACK_SHIFT_DIVISOR = 20
    }
}
