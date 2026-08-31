package com.quare.bibleplanner.feature.studysuggestion.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode

private val frameWidth = 72.dp
private val frameHeight = 108.dp
private val frameCornerRadius = 11.dp
private val lineHeight = 3.dp
private val lineCornerRadius = 2.dp
private val sheetHeight = 62.dp
private val sheetCornerRadius = 8.dp
private val sheetIconSize = 16.dp
private val sheetButtonHeight = 10.dp
private val bannerHeight = 16.dp
private val bannerCornerRadius = 6.dp
private val bannerDotSize = 9.dp
private val bottomBarHeight = 14.dp
private const val SCRIM_ALPHA = 0.42f
private const val SOFT_PRIMARY_ALPHA = 0.12f
private const val LINE_ALPHA = 0.35f

@Composable
internal fun StudySuggestionModePreview(
    mode: StudySuggestionMode,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(
                width = frameWidth,
                height = frameHeight,
            ).clip(RoundedCornerShape(frameCornerRadius))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(frameCornerRadius),
            ),
    ) {
        PreviewTextLines()
        if (mode == StudySuggestionMode.DIALOG) {
            DialogOverlay()
        } else {
            BannerOverlay()
        }
    }
}

@Composable
private fun PreviewTextLines() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 7.dp,
                vertical = 9.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        PreviewLine(widthFraction = 0.55f)
        PreviewLine(widthFraction = 0.9f)
        PreviewLine(widthFraction = 0.75f)
        PreviewLine(widthFraction = 0.85f)
        PreviewLine(widthFraction = 0.6f)
        PreviewLine(widthFraction = 0.8f)
    }
}

@Composable
private fun PreviewLine(widthFraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(lineHeight)
            .clip(RoundedCornerShape(lineCornerRadius))
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = LINE_ALPHA)),
    )
}

@Composable
private fun DialogOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = SCRIM_ALPHA)),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(sheetHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = sheetCornerRadius,
                        topEnd = sheetCornerRadius,
                    ),
                ).background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(
                    horizontal = 8.dp,
                    vertical = 6.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(sheetIconSize)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ),
                )
            }
            PreviewLine(widthFraction = 0.5f)
            PreviewLine(widthFraction = 0.8f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(sheetButtonHeight)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
private fun BannerOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .fillMaxWidth()
                    .height(bannerHeight)
                    .clip(RoundedCornerShape(bannerCornerRadius))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(bannerCornerRadius),
                    ).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(bannerDotSize)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = LINE_ALPHA)),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .fillMaxWidth()
                    .height(bottomBarHeight)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        ),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA)),
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
}
