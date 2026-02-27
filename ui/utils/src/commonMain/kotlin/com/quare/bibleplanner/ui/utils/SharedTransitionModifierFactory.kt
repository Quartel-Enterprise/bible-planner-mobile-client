package com.quare.bibleplanner.ui.utils

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object SharedTransitionModifierFactory {
    @Composable
    fun getBookNameSharedTransitionModifier(
        sharedTransitionScope: SharedTransitionScope,
        animatedVisibilityScope: AnimatedVisibilityScope,
        bookName: String,
    ): Modifier = with(sharedTransitionScope) {
        Modifier.sharedElement(
            rememberSharedContentState(key = "title-$bookName"),
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    @Composable
    fun getReadTopBarSharedTransitionBookChapterModifier(
        sharedTransitionScope: SharedTransitionScope,
        animatedVisibilityScope: AnimatedVisibilityScope,
        chapterNumber: Int,
        bookName: String,
    ): Modifier = with(sharedTransitionScope) {
        Modifier.sharedElement(
            rememberSharedContentState(key = "read-top-bar-shared-transition-$bookName-$chapterNumber"),
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}
