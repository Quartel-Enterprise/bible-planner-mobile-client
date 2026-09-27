package com.quare.bibleplanner.ui.utils.transition

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.defaultPopTransitionSpec
import androidx.navigation3.ui.defaultPredictivePopTransitionSpec
import androidx.navigation3.ui.defaultTransitionSpec

object CupertinoNavigationTransitions : NavigationTransitions {
    override fun createForwardTransition(scope: AnimatedContentTransitionScope<Scene<NavKey>>): ContentTransform =
        defaultTransitionSpec<NavKey>().invoke(scope)

    override fun createBackwardTransition(scope: AnimatedContentTransitionScope<Scene<NavKey>>): ContentTransform =
        defaultPopTransitionSpec<NavKey>().invoke(scope)

    override fun createPredictiveBackTransition(
        scope: AnimatedContentTransitionScope<Scene<NavKey>>,
        swipeEdge: Int,
    ): ContentTransform = defaultPredictivePopTransitionSpec<NavKey>().invoke(scope, swipeEdge)

    override fun createTabSwitchTransition(scope: AnimatedContentTransitionScope<Scene<NavKey>>): ContentTransform =
        EnterTransition.None togetherWith ExitTransition.None
}
