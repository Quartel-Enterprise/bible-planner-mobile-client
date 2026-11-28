package com.quare.bibleplanner.android.feature.annotations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

private const val PLACEHOLDER_ROWS = 5

@Composable
internal fun AnnotationsListSkeleton() {
    // Why: window bounds keep every placeholder in one sweep, as the shared screen's shimmer does.
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        repeat(PLACEHOLDER_ROWS) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnnotationsSkeletonBox(
                    shimmer = shimmer,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    AnnotationsSkeletonBox(
                        shimmer = shimmer,
                        modifier = Modifier.fillMaxWidth(0.4f).height(14.dp),
                    )
                    AnnotationsSkeletonBox(
                        shimmer = shimmer,
                        modifier = Modifier.fillMaxWidth().height(12.dp),
                    )
                    AnnotationsSkeletonBox(
                        shimmer = shimmer,
                        modifier = Modifier.fillMaxWidth(0.8f).height(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AnnotationsSkeletonBox(
    shimmer: Shimmer,
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmer(customShimmer = shimmer)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}
