package com.quare.bibleplanner.ui.utils

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.ui.utils.transition.sharedElementWithRelayout

object SharedTransitionModifierFactory {
    @Composable
    fun getBookNameSharedTransitionModifier(
        sharedTransitionScope: SharedTransitionScope,
        animatedVisibilityScope: AnimatedVisibilityScope,
        bookName: String,
    ): Modifier = with(sharedTransitionScope) {
        Modifier.sharedElement(
            rememberSharedContentState(key = buildBookNameKey(bookName)),
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    @Composable
    fun getTopBarBookNameSharedTransitionModifier(
        sharedTransitionScope: SharedTransitionScope,
        animatedVisibilityScope: AnimatedVisibilityScope,
        bookName: String,
    ): Modifier = Modifier.sharedElementWithRelayout(
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        key = buildBookNameKey(bookName),
    )

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

    private fun buildBookNameKey(bookName: String): String = "title-$bookName"
}
