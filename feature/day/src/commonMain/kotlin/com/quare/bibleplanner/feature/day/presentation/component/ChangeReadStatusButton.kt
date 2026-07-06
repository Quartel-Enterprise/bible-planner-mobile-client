package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.mark_as_read
import bibleplanner.feature.day.generated.resources.mark_as_unread
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import org.jetbrains.compose.resources.stringResource

private const val ANIMATION_TIME_MILLIS = 300
private val ButtonMinHeight = 44.dp

@Composable
internal fun ChangeReadStatusButton(
    isDayRead: Boolean,
    buttonModifier: Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = buttonModifier.heightIn(min = ButtonMinHeight),
        shape = RoundedCornerShape(percent = 50),
    ) {
        AnimatedContent(
            targetState = isDayRead,
            transitionSpec = { getTransitionSpec() },
            label = "read_status_transition",
        ) { dayRead ->
            ReadStatusContent(dayRead)
        }
    }
}

@Composable
private fun ReadStatusContent(dayRead: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = readStatusIcon(dayRead),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        HorizontalSpacer(ButtonDefaults.IconSpacing)
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
}

private fun readStatusIcon(dayRead: Boolean): ImageVector =
    if (dayRead) Icons.AutoMirrored.Rounded.Undo else Icons.Rounded.Check

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
