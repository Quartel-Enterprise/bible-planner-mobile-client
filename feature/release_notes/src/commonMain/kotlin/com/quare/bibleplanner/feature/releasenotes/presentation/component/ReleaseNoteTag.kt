package com.quare.bibleplanner.feature.releasenotes.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.release_notes.generated.resources.Res
import bibleplanner.feature.release_notes.generated.resources.release_notes_tag_latest
import bibleplanner.feature.release_notes.generated.resources.release_notes_tag_upcoming
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseVersionType
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReleaseNoteTag(
    type: ReleaseVersionType,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(
            if (type.isUpcoming()) {
                Res.string.release_notes_tag_upcoming
            } else {
                Res.string.release_notes_tag_latest
            },
        ),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = if (type.isUpcoming()) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
        ),
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (type.isUpcoming()) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
            ).padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

private fun ReleaseVersionType.isUpcoming(): Boolean = this == ReleaseVersionType.UPCOMING
