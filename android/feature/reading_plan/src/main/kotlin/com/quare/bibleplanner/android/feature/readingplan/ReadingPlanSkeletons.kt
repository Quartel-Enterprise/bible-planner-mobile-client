package com.quare.bibleplanner.android.feature.readingplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer

private const val HERO_PLACEHOLDER_ALPHA = 0.18f
private const val DAY_SKELETON_COUNT = 7
private val defaultSkeletonShape = RoundedCornerShape(8.dp)
private val pillShape = RoundedCornerShape(50)

@Composable
internal fun ReadingPlanHeroSkeleton(
    shimmer: Shimmer,
    modifier: Modifier = Modifier,
) {
    val placeholderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = HERO_PLACEHOLDER_ALPHA)
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SkeletonBox(shimmer, Modifier.width(110.dp).height(14.dp), color = placeholderColor)
            Column(
                modifier = Modifier.padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SkeletonBox(shimmer, Modifier.fillMaxWidth(0.85f).height(26.dp), color = placeholderColor)
                SkeletonBox(shimmer, Modifier.fillMaxWidth(0.55f).height(26.dp), color = placeholderColor)
            }
            SkeletonBox(shimmer, Modifier.width(120.dp).height(14.dp), color = placeholderColor)
            SkeletonBox(shimmer, Modifier.fillMaxWidth(0.7f).height(12.dp), color = placeholderColor)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SkeletonBox(
                    shimmer = shimmer,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = placeholderColor,
                )
                SkeletonBox(
                    shimmer = shimmer,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = placeholderColor,
                )
            }
        }
    }
}

@Composable
internal fun ReadingPlanProgressSkeleton(
    shimmer: Shimmer,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SkeletonBox(shimmer, Modifier.size(20.dp), shape = CircleShape)
                SkeletonBox(shimmer, Modifier.width(120.dp).height(18.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                SkeletonBox(shimmer, Modifier.size(100.dp), shape = CircleShape)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SkeletonBox(shimmer, Modifier.fillMaxWidth(0.6f).height(18.dp))
                    SkeletonBox(shimmer, Modifier.fillMaxWidth(0.4f).height(14.dp))
                    SkeletonBox(shimmer, Modifier.width(120.dp).height(28.dp), shape = pillShape)
                }
            }
            SkeletonBox(shimmer, Modifier.fillMaxWidth(0.7f).height(14.dp))
        }
    }
}

@Composable
internal fun ReadingPlanSectionHeaderSkeleton(shimmer: Shimmer) {
    SkeletonBox(
        shimmer = shimmer,
        modifier = Modifier
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 4.dp,
            ).width(90.dp)
            .height(12.dp),
    )
}

@Composable
internal fun ReadingPlanWeekSkeleton(
    shimmer: Shimmer,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        border = if (isCurrent) {
            BorderStroke(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            null
        },
    ) {
        WeekHeaderSkeleton(
            shimmer = shimmer,
            isCurrent = isCurrent,
        )
        if (isCurrent) {
            repeat(DAY_SKELETON_COUNT) {
                DaySkeletonRow(shimmer = shimmer)
            }
        }
    }
}

@Composable
private fun WeekHeaderSkeleton(
    shimmer: Shimmer,
    isCurrent: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SkeletonBox(shimmer, Modifier.width(80.dp).height(16.dp))
            if (isCurrent) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SkeletonBox(shimmer, Modifier.width(70.dp).height(12.dp))
                    SkeletonBox(shimmer, Modifier.weight(1f).height(6.dp), shape = pillShape)
                }
            } else {
                SkeletonBox(shimmer, Modifier.fillMaxWidth(0.4f).height(12.dp))
            }
        }
        SkeletonBox(shimmer, Modifier.size(24.dp), shape = CircleShape)
    }
}

@Composable
private fun DaySkeletonRow(shimmer: Shimmer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = 12.dp,
                    end = 8.dp,
                    top = 12.dp,
                    bottom = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                SkeletonBox(shimmer, Modifier.width(22.dp).height(16.dp))
                SkeletonBox(shimmer, Modifier.width(18.dp).height(10.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SkeletonBox(shimmer, Modifier.fillMaxWidth(0.45f).height(16.dp))
                SkeletonBox(shimmer, Modifier.fillMaxWidth(0.65f).height(12.dp))
            }
            SkeletonBox(shimmer, Modifier.size(36.dp), shape = CircleShape)
            SkeletonBox(shimmer, Modifier.width(8.dp).height(14.dp))
        }
    }
}

@Composable
private fun SkeletonBox(
    shimmer: Shimmer,
    modifier: Modifier = Modifier,
    shape: Shape = defaultSkeletonShape,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmer(customShimmer = shimmer)
            .background(color),
    )
}
