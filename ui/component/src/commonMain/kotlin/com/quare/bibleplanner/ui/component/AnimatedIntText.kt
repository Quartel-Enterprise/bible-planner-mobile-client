package com.quare.bibleplanner.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun AnimatedIntText(
    modifier: Modifier = Modifier,
    value: Int,
    label: String,
    animationDurationMs: Int = 300,
    style: TextStyle = LocalTextStyle.current,
) {
    AnimatedContent(
        modifier = modifier,
        targetState = value,
        transitionSpec = {
            getProgressAnimation(animationDurationMs)
        },
        label = label,
    ) { count ->
        Text(
            text = count.toString(),
            style = style,
        )
    }
}

private fun getProgressAnimation(durationMs: Int): ContentTransform = (
    fadeIn(animationSpec = tween(durationMs)) + slideInVertically(
        animationSpec = tween(durationMs),
        initialOffsetY = { it },
    )
) togetherWith (
    fadeOut(animationSpec = tween(durationMs)) + slideOutVertically(
        animationSpec = tween(durationMs),
        targetOffsetY = { -it },
    )
)
