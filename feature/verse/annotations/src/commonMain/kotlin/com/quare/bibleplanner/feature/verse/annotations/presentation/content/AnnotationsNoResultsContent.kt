package com.quare.bibleplanner.feature.verse.annotations.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.no_results_clear
import bibleplanner.feature.verse.annotations.generated.resources.no_results_filters_hint
import bibleplanner.feature.verse.annotations.generated.resources.no_results_query
import bibleplanner.feature.verse.annotations.generated.resources.no_results_query_hint
import bibleplanner.feature.verse.annotations.generated.resources.no_results_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AnnotationsNoResultsContent(
    query: String,
    onClearFiltersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 24.dp,
                vertical = 60.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = if (query.isEmpty()) Icons.Default.FilterAltOff else Icons.Default.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = if (query.isEmpty()) {
                stringResource(Res.string.no_results_title)
            } else {
                stringResource(Res.string.no_results_query, query)
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(
                if (query.isEmpty()) Res.string.no_results_filters_hint else Res.string.no_results_query_hint,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
        TextButton(onClick = onClearFiltersClick) {
            Text(text = stringResource(Res.string.no_results_clear))
        }
    }
}
