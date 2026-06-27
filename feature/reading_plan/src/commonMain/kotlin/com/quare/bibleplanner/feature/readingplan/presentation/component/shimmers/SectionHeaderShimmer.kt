package com.quare.bibleplanner.feature.readingplan.presentation.component.shimmers

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox

@Composable
internal fun SectionHeaderShimmer(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 4.dp,
            ).width(90.dp)
            .height(12.dp),
    )
}
