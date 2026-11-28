package com.quare.bibleplanner.android.feature.read

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import software.quare.loadable.Loadable

private val abbreviationShimmerWidth = 32.dp
private val abbreviationShimmerHeight = 14.dp

@Composable
internal fun BibleVersionChip(
    versionName: Loadable<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription = stringResource(R.string.read_change_bible_version)
    AssistChip(
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        onClick = onClick,
        label = {
            when (versionName) {
                Loadable.Loading -> {
                    val pulse by rememberSkeletonPulse()
                    ReadSkeletonBox(
                        modifier = Modifier
                            .graphicsLayer { alpha = pulse }
                            .width(abbreviationShimmerWidth)
                            .height(abbreviationShimmerHeight),
                    )
                }

                is Loadable.Loaded -> Text(text = versionName.value)
            }
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
            )
        },
    )
}
