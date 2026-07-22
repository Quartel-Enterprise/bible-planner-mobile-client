package com.quare.bibleplanner.feature.readingplan.presentation.component.shimmers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox

private const val HERO_PLACEHOLDER_ALPHA = 0.18f

@Composable
internal fun PlanHeroShimmer(modifier: Modifier = Modifier) {
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
            ShimmerBox(
                modifier = Modifier.width(110.dp).height(14.dp),
                color = placeholderColor,
            )
            Column(
                modifier = Modifier.padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ShimmerBox(
                    modifier = Modifier.fillMaxWidth(0.85f).height(26.dp),
                    color = placeholderColor,
                )
                ShimmerBox(
                    modifier = Modifier.fillMaxWidth(0.55f).height(26.dp),
                    color = placeholderColor,
                )
            }
            ShimmerBox(
                modifier = Modifier.width(120.dp).height(14.dp),
                color = placeholderColor,
            )
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.7f).height(12.dp),
                color = placeholderColor,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = placeholderColor,
                )
                ShimmerBox(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = placeholderColor,
                )
            }
        }
    }
}
