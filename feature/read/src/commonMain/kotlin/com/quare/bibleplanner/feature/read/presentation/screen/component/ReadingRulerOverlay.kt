package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.dismiss_ruler
import org.jetbrains.compose.resources.stringResource

private const val SCRIM_ALPHA = 0.45f
private const val TOP_WEIGHT = 0.34f
private const val BOTTOM_WEIGHT = 0.66f
private val bandExtraHeight = 14.dp
private val dismissButtonSize = 28.dp

/**
 * Dims everything but a band the height of one line, so the eye has somewhere to sit while
 * scrolling. Purely decorative: it never intercepts taps on the verses underneath.
 */
@Composable
internal fun ReadingRulerOverlay(
    lineHeight: Dp,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA)
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(TOP_WEIGHT)
                .background(scrimColor),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(lineHeight + bandExtraHeight),
            contentAlignment = Alignment.CenterEnd,
        ) {
            FilledIconButton(
                modifier = Modifier.size(dismissButtonSize),
                onClick = onDismiss,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.dismiss_ruler),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(BOTTOM_WEIGHT)
                .background(scrimColor),
        )
    }
}
