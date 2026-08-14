package com.quare.bibleplanner.feature.chat.presentation.component.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chat.generated.resources.Res
import bibleplanner.feature.chat.generated.resources.chat_cancel
import bibleplanner.feature.chat.generated.resources.chat_confirm_rename
import bibleplanner.feature.chat.generated.resources.chat_conversation_options
import bibleplanner.feature.chat.generated.resources.chat_delete
import bibleplanner.feature.chat.generated.resources.chat_delete_confirmation
import bibleplanner.feature.chat.generated.resources.chat_rename
import com.quare.bibleplanner.feature.chat.presentation.model.ChatConversationUiModel
import com.quare.bibleplanner.ui.utils.format
import org.jetbrains.compose.resources.stringResource

private val rowShape = RoundedCornerShape(12.dp)
private val actionIconSize = 20.dp

@Composable
internal fun ChatHistoryRow(
    conversation: ChatConversationUiModel,
    areActionsExpanded: Boolean,
    isRenaming: Boolean,
    renameDraft: String,
    isDeleting: Boolean,
    onClick: () -> Unit,
    onActionsToggle: () -> Unit,
    onRenameClick: () -> Unit,
    onRenameDraftChange: (String) -> Unit,
    onRenameConfirm: () -> Unit,
    onRenameCancel: () -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(
                if (conversation.isActive) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ).padding(
                horizontal = 12.dp,
                vertical = 10.dp,
            ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isRenaming) {
                OutlinedTextField(
                    value = renameDraft,
                    onValueChange = onRenameDraftChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                IconButton(onClick = onRenameConfirm) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = stringResource(Res.string.chat_confirm_rename),
                    )
                }
                IconButton(onClick = onRenameCancel) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(Res.string.chat_cancel),
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onClick),
                ) {
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    conversation.preview?.let { preview ->
                        Text(
                            text = preview,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Text(
                    text = conversation.relativeTime.format(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                IconButton(onClick = onActionsToggle) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(Res.string.chat_conversation_options),
                        modifier = Modifier.size(actionIconSize),
                    )
                }
            }
        }
        AnimatedVisibility(areActionsExpanded && !isRenaming && !isDeleting) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onRenameClick) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(actionIconSize),
                    )
                    Text(
                        text = stringResource(Res.string.chat_rename),
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
                TextButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(actionIconSize),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = stringResource(Res.string.chat_delete),
                        modifier = Modifier.padding(start = 6.dp),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        AnimatedVisibility(isDeleting) {
            ChatDeleteConfirmStrip(
                onConfirm = onDeleteConfirm,
                onCancel = onDeleteCancel,
            )
        }
    }
}

@Composable
private fun ChatDeleteConfirmStrip(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(
                horizontal = 10.dp,
                vertical = 4.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.chat_delete_confirmation),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        TextButton(onClick = onCancel) {
            Text(stringResource(Res.string.chat_cancel))
        }
        TextButton(onClick = onConfirm) {
            Text(
                text = stringResource(Res.string.chat_delete),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
