package com.quare.bibleplanner.android.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.profile.presentation.model.AnnotationsSummaryModel
import com.quare.bibleplanner.feature.profile.presentation.model.OtherVersionAnnotationCountModel
import software.quare.loadable.Loadable

private const val ICON_BACKGROUND_ALPHA = 0.12f
private const val SUMMARY_SEPARATOR = " · "

@Composable
internal fun ProfileAnnotationsCard(
    summary: Loadable<AnnotationsSummaryModel>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = ICON_BACKGROUND_ALPHA),
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmarks,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_annotations_option),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                when (summary) {
                    Loadable.Loading -> ProfileSkeletonBox(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(0.7f)
                            .height(12.dp),
                    )

                    is Loadable.Loaded -> {
                        Text(
                            text = summary.value.summaryText(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (summary.value.otherVersions.isNotEmpty()) {
                            Text(
                                text = summary.value.otherVersions.otherVersionsText(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun List<OtherVersionAnnotationCountModel>.otherVersionsText(): String = map { otherVersion ->
    pluralStringResource(
        R.plurals.profile_annotations_other_version_count,
        otherVersion.count,
        otherVersion.count,
        otherVersion.versionAbbreviation,
    )
}.joinToString(separator = SUMMARY_SEPARATOR)

@Composable
private fun AnnotationsSummaryModel.summaryText(): String {
    if (highlightCount + savedCount + noteCount == 0) return stringResource(R.string.profile_annotations_summary_empty)
    return listOf(
        pluralStringResource(R.plurals.profile_annotations_highlight_count, highlightCount, highlightCount),
        pluralStringResource(R.plurals.profile_annotations_saved_count, savedCount, savedCount),
        pluralStringResource(R.plurals.profile_annotations_note_count, noteCount, noteCount),
    ).joinToString(separator = SUMMARY_SEPARATOR)
}
