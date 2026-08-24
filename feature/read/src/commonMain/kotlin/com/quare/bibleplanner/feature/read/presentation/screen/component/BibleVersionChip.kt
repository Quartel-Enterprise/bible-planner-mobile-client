package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.change_bible_version
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox
import org.jetbrains.compose.resources.stringResource

private val abbreviationShimmerWidth = 32.dp
private val abbreviationShimmerHeight = 14.dp

@Composable
internal fun BibleVersionChip(
    versionName: Loadable<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription = stringResource(Res.string.change_bible_version)
    AssistChip(
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        onClick = onClick,
        label = {
            when (versionName) {
                Loadable.Loading -> {
                    ShimmerBox(
                        modifier = Modifier
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
