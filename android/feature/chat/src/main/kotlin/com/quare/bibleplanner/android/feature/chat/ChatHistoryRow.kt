package com.quare.bibleplanner.android.feature.chat

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
import androidx.compose.foundation.text.input.TextFieldLineLimits
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.text.rememberSyncedTextFieldState
import com.quare.bibleplanner.feature.chat.presentation.model.ChatConversationUiModel
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiEvent

private val rowShape = RoundedCornerShape(12.dp)
private val actionIconSize = 20.dp

@Composable
internal fun ChatHistoryRow(
    conversation: ChatConversationUiModel,
    areActionsExpanded: Boolean,
    isRenaming: Boolean,
    renameDraft: String,
    isDeleting: Boolean,
    onEvent: (ChatUiEvent) -> Unit,
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
        if (isRenaming) {
            ChatRenameRow(
                renameDraft = renameDraft,
                onEvent = onEvent,
            )
        } else {
            ChatConversationSummaryRow(
                conversation = conversation,
                onEvent = onEvent,
            )
        }
        AnimatedVisibility(areActionsExpanded && !isRenaming && !isDeleting) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onEvent(ChatUiEvent.OnRenameConversationClick(conversation.id)) }) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(actionIconSize),
                    )
                    Text(
                        text = stringResource(R.string.chat_rename),
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
                TextButton(onClick = { onEvent(ChatUiEvent.OnDeleteConversationClick(conversation.id)) }) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(actionIconSize),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = stringResource(R.string.chat_delete),
                        modifier = Modifier.padding(start = 6.dp),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        AnimatedVisibility(isDeleting) {
            ChatDeleteConfirmRow(onEvent = onEvent)
        }
    }
}

@Composable
private fun ChatConversationSummaryRow(
    conversation: ChatConversationUiModel,
    onEvent: (ChatUiEvent) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onEvent(ChatUiEvent.OnConversationClick(conversation.id)) },
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
            text = relativeTimeLabel(conversation.relativeTime),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
        IconButton(onClick = { onEvent(ChatUiEvent.OnConversationActionsToggle(conversation.id)) }) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.chat_conversation_options),
                modifier = Modifier.size(actionIconSize),
            )
        }
    }
}

@Composable
private fun ChatRenameRow(
    renameDraft: String,
    onEvent: (ChatUiEvent) -> Unit,
) {
    val synced = rememberSyncedTextFieldState(
        value = renameDraft,
        onValueChange = { title -> onEvent(ChatUiEvent.OnRenameDraftChanged(title)) },
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            state = synced.textFieldState,
            modifier = Modifier.weight(1f),
            lineLimits = TextFieldLineLimits.SingleLine,
        )
        IconButton(
            onClick = {
                synced.sendLatest()
                onEvent(ChatUiEvent.OnRenameConfirm)
            },
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(R.string.chat_confirm_rename),
            )
        }
        IconButton(onClick = { onEvent(ChatUiEvent.OnRenameCancel) }) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.chat_cancel),
            )
        }
    }
}

@Composable
private fun ChatDeleteConfirmRow(onEvent: (ChatUiEvent) -> Unit) {
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
            text = stringResource(R.string.chat_delete_confirmation),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        TextButton(onClick = { onEvent(ChatUiEvent.OnDeleteCancel) }) {
            Text(stringResource(R.string.chat_cancel))
        }
        TextButton(onClick = { onEvent(ChatUiEvent.OnDeleteConfirm) }) {
            Text(
                text = stringResource(R.string.chat_delete),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
