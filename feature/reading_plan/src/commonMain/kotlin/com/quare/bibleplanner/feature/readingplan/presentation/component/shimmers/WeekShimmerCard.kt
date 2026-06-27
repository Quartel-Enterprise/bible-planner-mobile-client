package com.quare.bibleplanner.feature.readingplan.presentation.component.shimmers

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox

@Composable
internal fun WeekShimmerCard(
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
        Column(modifier = Modifier.fillMaxWidth()) {
            WeekHeaderShimmer(isCurrent = isCurrent)
            if (isCurrent) {
                repeat(DAY_SHIMMER_COUNT) {
                    DayShimmerRow()
                }
            }
        }
    }
}

@Composable
private fun WeekHeaderShimmer(isCurrent: Boolean) {
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
            ShimmerBox(modifier = Modifier.width(80.dp).height(16.dp))
            if (isCurrent) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ShimmerBox(modifier = Modifier.width(70.dp).height(12.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp),
                        shape = RoundedCornerShape(50),
                    )
                }
            } else {
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp))
            }
        }
        ShimmerBox(
            modifier = Modifier.size(24.dp),
            shape = CircleShape,
        )
    }
}

@Composable
private fun DayShimmerRow() {
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
                    top = 10.dp,
                    bottom = 10.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                ShimmerBox(modifier = Modifier.width(24.dp).height(16.dp))
                ShimmerBox(modifier = Modifier.width(20.dp).height(10.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp))
            }
            ShimmerBox(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
            )
            ShimmerBox(modifier = Modifier.width(8.dp).height(14.dp))
        }
    }
}

private const val DAY_SHIMMER_COUNT = 3
