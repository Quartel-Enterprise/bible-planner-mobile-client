package com.quare.bibleplanner.feature.profile.presentation.content.component

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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.annotations_highlight_count
import bibleplanner.feature.profile.generated.resources.annotations_note_count
import bibleplanner.feature.profile.generated.resources.annotations_option
import bibleplanner.feature.profile.generated.resources.annotations_saved_count
import bibleplanner.feature.profile.generated.resources.annotations_summary_empty
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.feature.profile.presentation.model.AnnotationsSummaryModel
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox
import com.quare.bibleplanner.ui.icons.AppIcon
import com.quare.bibleplanner.ui.icons.Icon
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val ICON_BACKGROUND_ALPHA = 0.12f
private const val SUMMARY_SEPARATOR = " · "

@Composable
internal fun AnnotationsCard(
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
                    icon = AppIcon.Bookmarks,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.annotations_option),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                when (summary) {
                    Loadable.Loading -> ShimmerBox(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(0.7f)
                            .height(12.dp),
                    )

                    is Loadable.Loaded -> Text(
                        text = summary.value.toText(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                icon = AppIcon.ChevronForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AnnotationsSummaryModel.toText(): String {
    if (highlightCount + savedCount + noteCount == 0) return stringResource(Res.string.annotations_summary_empty)
    return listOf(
        pluralStringResource(
            Res.plurals.annotations_highlight_count,
            highlightCount,
            highlightCount,
        ),
        pluralStringResource(
            Res.plurals.annotations_saved_count,
            savedCount,
            savedCount,
        ),
        pluralStringResource(
            Res.plurals.annotations_note_count,
            noteCount,
            noteCount,
        ),
    ).joinToString(separator = SUMMARY_SEPARATOR)
}
