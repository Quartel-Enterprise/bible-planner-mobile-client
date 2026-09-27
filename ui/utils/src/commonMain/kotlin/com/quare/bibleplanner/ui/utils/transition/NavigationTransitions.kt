package com.quare.bibleplanner.ui.utils.transition

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

interface NavigationTransitions {
    fun createForwardTransition(scope: AnimatedContentTransitionScope<Scene<NavKey>>): ContentTransform

    fun createBackwardTransition(scope: AnimatedContentTransitionScope<Scene<NavKey>>): ContentTransform

    fun createPredictiveBackTransition(
        scope: AnimatedContentTransitionScope<Scene<NavKey>>,
        swipeEdge: Int,
    ): ContentTransform

    fun createTabSwitchTransition(scope: AnimatedContentTransitionScope<Scene<NavKey>>): ContentTransform
}
