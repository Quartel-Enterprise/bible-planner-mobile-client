package com.quare.bibleplanner.feature.releasenotes.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.release_notes.generated.resources.Res
import bibleplanner.feature.release_notes.generated.resources.release_notes_card_more_details
import bibleplanner.feature.release_notes.generated.resources.release_notes_tag_latest
import bibleplanner.feature.release_notes.generated.resources.release_notes_tag_upcoming
import bibleplanner.ui.component.generated.resources.ic_github
import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.utils.format
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import bibleplanner.ui.component.generated.resources.Res as ComponentRes

@Composable
fun ReleaseNoteCard(
    modifier: Modifier = Modifier,
    note: ReleaseNoteModel,
    isLatest: Boolean = false,
    isUpcoming: Boolean = false,
    onGithubClick: (String) -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = note.version,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    note.dateRepresentation?.let { representation ->
                        Text(
                            text = representation.format(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLatest || isUpcoming) {
                        val tagText = if (isUpcoming) {
                            stringResource(Res.string.release_notes_tag_upcoming)
                        } else {
                            stringResource(Res.string.release_notes_tag_latest)
                        }
                        val tagColor = if (isUpcoming) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                        val onTagColor = if (isUpcoming) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }

                        Text(
                            text = tagText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = onTagColor,
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(tagColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (!isUpcoming) {
                        CommonIconButton(
                            painter = painterResource(ComponentRes.drawable.ic_github),
                            contentDescription = stringResource(Res.string.release_notes_card_more_details),
                            onClick = { onGithubClick(note.version) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            note.changes.forEach { change ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = change,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}
