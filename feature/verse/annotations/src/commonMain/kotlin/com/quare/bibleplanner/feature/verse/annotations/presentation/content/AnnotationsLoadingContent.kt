package com.quare.bibleplanner.feature.verse.annotations.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox

private const val PLACEHOLDER_ROWS = 5

@Composable
internal fun AnnotationsLoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        repeat(PLACEHOLDER_ROWS) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShimmerBox(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth().height(12.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.8f).height(12.dp))
                }
            }
        }
    }
}
