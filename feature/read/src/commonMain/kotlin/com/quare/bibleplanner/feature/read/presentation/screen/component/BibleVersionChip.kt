package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.change_bible_version
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BibleVersionChip(
    versionName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription = stringResource(Res.string.change_bible_version)
    AssistChip(
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        onClick = onClick,
        label = { Text(text = versionName) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
            )
        },
    )
}
