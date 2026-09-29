package com.quare.bibleplanner.feature.verse.annotations.presentation.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.empty_body
import bibleplanner.feature.verse.annotations.generated.resources.empty_title
import bibleplanner.feature.verse.annotations.generated.resources.other_versions_empty_body
import bibleplanner.feature.verse.annotations.generated.resources.other_versions_empty_title
import com.quare.bibleplanner.feature.verse.annotations.presentation.component.OtherVersionsCard
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.OtherVersionAnnotationsUiModel
import org.jetbrains.compose.resources.stringResource

private const val ICON_BACKGROUND_ALPHA = 0.12f

@Composable
internal fun AnnotationsEmptyContent(
    otherVersions: List<OtherVersionAnnotationsUiModel>,
    onEvent: (AnnotationsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasOtherVersions = otherVersions.isNotEmpty()
    Column(
        modifier = modifier
            .fillMaxWidth()
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
                if (hasOtherVersions) Res.string.other_versions_empty_title else Res.string.empty_title,
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = stringResource(
                if (hasOtherVersions) Res.string.other_versions_empty_body else Res.string.empty_body,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (hasOtherVersions) {
            OtherVersionsCard(
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
