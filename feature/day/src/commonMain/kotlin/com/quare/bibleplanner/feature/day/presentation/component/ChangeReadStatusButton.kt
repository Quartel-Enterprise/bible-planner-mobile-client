package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.mark_as_read
import bibleplanner.feature.day.generated.resources.mark_as_unread
import org.jetbrains.compose.resources.stringResource

private const val ANIMATION_TIME_MILLIS = 300

@Composable
internal fun ChangeReadStatusButton(
    isDayRead: Boolean,
    buttonModifier: Modifier,
    onClick: () -> Unit,
) {
    AnimatedContent(
        modifier = buttonModifier,
        targetState = isDayRead,
        transitionSpec = { getTransitionSpec() },
        label = "button_transition",
    ) { dayRead ->
        val content: @Composable RowScope.() -> Unit = {
            ReadStatusText(dayRead)
        }

        if (dayRead) {
            OutlinedButton(
                onClick = onClick,
                content = content,
            )
        } else {
            Button(
                onClick = onClick,
                content = content,
            )
        }
    }
}

@Composable
private fun ReadStatusText(dayRead: Boolean) {
    Text(
        text = stringResource(
            if (dayRead) {
                Res.string.mark_as_unread
            } else {
                Res.string.mark_as_read
            },
        ),
    )
}

private fun getTransitionSpec(): ContentTransform = (
    fadeIn(animationSpec = tween(ANIMATION_TIME_MILLIS)) + slideInVertically(
        animationSpec = tween(ANIMATION_TIME_MILLIS),
        initialOffsetY = { it / 2 },
    )
) togetherWith (
    fadeOut(animationSpec = tween(ANIMATION_TIME_MILLIS)) + slideOutVertically(
        animationSpec = tween(ANIMATION_TIME_MILLIS),
        targetOffsetY = { -it / 2 },
    )
)
