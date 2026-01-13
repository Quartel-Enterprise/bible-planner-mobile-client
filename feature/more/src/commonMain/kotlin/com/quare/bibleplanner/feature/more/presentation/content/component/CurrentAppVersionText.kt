package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.app_version_disclaimer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CurrentAppVersionText(
    modifier: Modifier = Modifier,
    appVersion: String,
) {
    Text(
        modifier = modifier,
        text = stringResource(Res.string.app_version_disclaimer, appVersion),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}
