package com.quare.bibleplanner.feature.readingplan.presentation.component.shimmers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
internal fun PlanProgressShimmer(modifier: Modifier = Modifier) {
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
            ShimmerBox(modifier = Modifier.width(160.dp).height(18.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                ShimmerBox(
                    modifier = Modifier.size(92.dp),
                    shape = CircleShape,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(18.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp))
                    ShimmerBox(
                        modifier = Modifier.width(120.dp).height(24.dp),
                        shape = RoundedCornerShape(50),
                    )
                }
            }
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(14.dp))
        }
    }
}
