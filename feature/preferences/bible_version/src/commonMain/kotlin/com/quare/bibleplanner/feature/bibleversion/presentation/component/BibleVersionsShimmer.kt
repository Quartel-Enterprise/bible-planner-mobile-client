package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox

private const val SHIMMER_GROUP_COUNT = 2
private const val SHIMMER_ITEMS_PER_GROUP = 1

internal fun LazyListScope.bibleVersionsShimmer() {
    repeat(SHIMMER_GROUP_COUNT) {
        item {
            ShimmerBox(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(96.dp)
                    .height(14.dp),
            )
        }
        items(SHIMMER_ITEMS_PER_GROUP) {
            BibleVersionItemShimmer(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun BibleVersionItemShimmer(modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier.clip(MaterialTheme.shapes.medium),
        leadingContent = {
            ShimmerBox(
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
            )
        },
        headlineContent = {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(18.dp),
            )
        },
        supportingContent = {
            ShimmerBox(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth(0.3f)
                    .height(12.dp),
            )
        },
        trailingContent = {
            ShimmerBox(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
            )
        },
    )
}
