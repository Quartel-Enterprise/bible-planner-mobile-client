package com.quare.bibleplanner.android.feature.annotations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.OtherVersionAnnotationsUiModel

@Composable
internal fun AnnotationsOtherVersionsCard(
    otherVersions: List<OtherVersionAnnotationsUiModel>,
    isEmptyState: Boolean,
    onEvent: (AnnotationsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 8.dp,
                top = 4.dp,
                bottom = 4.dp,
            ),
        ) {
            otherVersions.forEach { otherVersion ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.annotations_other_version_count,
                            otherVersion.count,
                            otherVersion.count,
                            otherVersion.versionAbbreviation,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = {
                            onEvent(
                                AnnotationsUiEvent.OnUseVersionClick(
                                    bibleVersionId = otherVersion.bibleVersionId,
                                    isEmptyState = isEmptyState,
                                ),
                            )
                        },
                    ) {
                        Text(
                            text = stringResource(
                                R.string.annotations_other_version_use,
                                otherVersion.versionAbbreviation,
                            ),
                        )
                    }
                }
            }
        }
    }
}
