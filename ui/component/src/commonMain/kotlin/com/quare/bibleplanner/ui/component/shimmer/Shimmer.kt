package com.quare.bibleplanner.ui.component.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer as valentinilkShimmer

@Composable
fun Modifier.shimmer(): Modifier {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
    return valentinilkShimmer(customShimmer = shimmer)
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(SHIMMER_CORNER_RADIUS.dp),
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmer()
            .background(color),
    )
}

private const val SHIMMER_CORNER_RADIUS = 8
