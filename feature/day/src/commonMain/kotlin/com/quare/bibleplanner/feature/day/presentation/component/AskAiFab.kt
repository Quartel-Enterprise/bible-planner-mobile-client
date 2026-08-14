package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.day_ask_ai
import org.jetbrains.compose.resources.stringResource

private val sparkleSize = 22.dp

/** What the scrolled content leaves free below itself so the button never rests on its last row. */
internal val askAiFabClearance = 88.dp

@Composable
internal fun AskAiFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(sparkleSize),
        )
        Text(
            text = stringResource(Res.string.day_ask_ai),
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}
