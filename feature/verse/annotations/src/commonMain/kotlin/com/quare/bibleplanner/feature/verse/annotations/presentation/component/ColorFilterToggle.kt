package com.quare.bibleplanner.feature.verse.annotations.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.color_filter
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.ColorFilterUiModel
import com.quare.bibleplanner.ui.component.highlight.toSwatchColor
import org.jetbrains.compose.resources.stringResource

private const val EMPTY_ALPHA = 0.25f
private const val UNSELECTED_ALPHA = 0.45f
private val checkInk = Color(0xA6000000)

@Composable
internal fun ColorFilterToggle(
    filter: ColorFilterUiModel,
    isAnyColorSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val swatch = filter.color.toSwatchColor()
    val alpha = when {
        filter.isSelected -> 1f
        filter.count == 0 -> EMPTY_ALPHA
        isAnyColorSelected -> UNSELECTED_ALPHA
        else -> 1f
    }
    val description = stringResource(Res.string.color_filter)
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .toggleable(
                value = filter.isSelected,
                role = Role.Checkbox,
                onValueChange = { onClick() },
            ).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .alpha(alpha)
                .then(
                    if (filter.isSelected) {
                        Modifier.border(
                            width = 2.dp,
                            color = swatch,
                            shape = CircleShape,
                        )
                    } else {
                        Modifier
                    },
                ).padding(if (filter.isSelected) 4.dp else 0.dp)
                .size(26.dp)
                .background(
                    color = swatch,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (filter.isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = checkInk,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}
