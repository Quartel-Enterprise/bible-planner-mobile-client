package com.quare.bibleplanner.android.feature.annotations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.OtherVersionAnnotationsUiModel

private const val ICON_BACKGROUND_ALPHA = 0.12f

@Composable
internal fun AnnotationsEmptyContent(
    otherVersions: List<OtherVersionAnnotationsUiModel>,
    onEvent: (AnnotationsUiEvent) -> Unit,
) {
    val hasOtherVersions = otherVersions.isNotEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 24.dp,
                vertical = 64.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = ICON_BACKGROUND_ALPHA),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.BorderColor,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp),
            )
        }
        Text(
            text = stringResource(
                if (hasOtherVersions) {
                    R.string.annotations_other_versions_empty_title
                } else {
                    R.string.annotations_empty_title
                },
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = stringResource(
                if (hasOtherVersions) {
                    R.string.annotations_other_versions_empty_body
                } else {
                    R.string.annotations_empty_body
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (hasOtherVersions) {
            AnnotationsOtherVersionsCard(
                otherVersions = otherVersions,
                isEmptyState = true,
                onEvent = onEvent,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .widthIn(max = 420.dp),
            )
        }
    }
}

@Composable
internal fun AnnotationsNoResultsContent(
    query: String,
    onClearFiltersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
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
                stringResource(R.string.annotations_no_results_title)
            } else {
                stringResource(R.string.annotations_no_results_query, query)
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(
                if (query.isEmpty()) {
                    R.string.annotations_no_results_filters_hint
                } else {
                    R.string.annotations_no_results_query_hint
                },
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
        TextButton(onClick = onClearFiltersClick) {
            Text(text = stringResource(R.string.annotations_no_results_clear))
        }
    }
}
