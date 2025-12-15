package com.quare.bibleplanner.ui.component.icon

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.collapse
import bibleplanner.ui.component.generated.resources.expand
import org.jetbrains.compose.resources.stringResource

private const val EXPANDED_ROTATION_VALUE = 180f
private const val COLLAPSED_ROTATION_VALUE = 0f
private const val ROTATION_LABEL = "WeekArrowRotation"

@Composable
fun ArrowRotationIcon(
    modifier: Modifier = Modifier,
    isUp: Boolean,
) {
    val arrowRotation by getAnimationState(isUp)
    Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = stringResource(
            resource = if (isUp) {
                Res.string.collapse
            } else {
                Res.string.expand
            },
        ),
        modifier = modifier
            .rotate(arrowRotation),
    )
}

@Composable
private fun getAnimationState(isUp: Boolean): State<Float> = animateFloatAsState(
    targetValue = if (isUp) {
        EXPANDED_ROTATION_VALUE
    } else {
        COLLAPSED_ROTATION_VALUE
    },
    label = ROTATION_LABEL,
)
