package com.quare.bibleplanner.feature.chat.presentation.component.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chat.generated.resources.Res
import bibleplanner.feature.chat.generated.resources.chat_back
import bibleplanner.feature.chat.generated.resources.chat_bucket_last_seven_days
import bibleplanner.feature.chat.generated.resources.chat_bucket_today
import bibleplanner.feature.chat.generated.resources.chat_bucket_yesterday
import bibleplanner.feature.chat.generated.resources.chat_conversations
import bibleplanner.feature.chat.generated.resources.chat_no_conversations_found
import bibleplanner.feature.chat.generated.resources.chat_search_conversations
import com.quare.bibleplanner.feature.chat.presentation.model.ChatConversationBucket
import com.quare.bibleplanner.feature.chat.presentation.model.ChatHistoryUiState
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiEvent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.utils.toStringResource
import org.jetbrains.compose.resources.stringResource

private val emptyStateIconSize = 36.dp

/**
 * The conversation list itself, without the surface it sits on: slid in over the thread on a narrow
 * window, permanently beside it on a wide one. Which of [onNavigateBack] and [onDismiss] is given
 * is what tells the two apart — a sidebar leads the title with the way out of the chat, a drawer
 * trails it with the way back to the thread.
 */
@Composable
internal fun ChatHistoryPane(
    history: ChatHistoryUiState,
    onEvent: (ChatUiEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
    onDismiss: (() -> Unit)?,
    contentPadding: PaddingValues,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (onNavigateBack == null) 16.dp else 4.dp,
                    end = 8.dp,
                    top = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            onNavigateBack?.let { navigateBack ->
                IconButton(onClick = navigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(Res.string.chat_back),
                    )
                }
            }
            Text(
                text = stringResource(Res.string.chat_conversations),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            onDismiss?.let { dismiss ->
                IconButton(onClick = dismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(Res.string.chat_conversations),
                    )
                }
            }
        }
        OutlinedTextField(
            value = history.query,
            onValueChange = { query -> onEvent(ChatUiEvent.OnHistoryQueryChanged(query)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(stringResource(Res.string.chat_search_conversations)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                )
            },
            trailingIcon = {
                if (history.query.isNotEmpty()) {
                    IconButton(onClick = { onEvent(ChatUiEvent.OnHistoryQueryChanged("")) }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                        )
                    }
                }
            },
            singleLine = true,
        )
        VerticalSpacer(8)
        if (history.groups.isEmpty() && history.hasConversations) {
            EmptySearchState()
        } else {
            ConversationList(
                history = history,
                onEvent = onEvent,
                contentPadding = contentPadding,
            )
        }
    }
}

@Composable
private fun ConversationList(
    history: ChatHistoryUiState,
    onEvent: (ChatUiEvent) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        history.groups.forEach { group ->
            item(key = group.bucket.toString()) {
                Text(
                    text = group.bucket.label(),
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 12.dp,
                        bottom = 4.dp,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            items(
                items = group.conversations,
                key = { conversation -> conversation.id },
            ) { conversation ->
                ChatHistoryRow(
                    conversation = conversation,
                    areActionsExpanded = history.expandedActionsId == conversation.id,
                    isRenaming = history.renamingId == conversation.id,
                    renameDraft = history.renameDraft,
                    isDeleting = history.deletingId == conversation.id,
                    onClick = { onEvent(ChatUiEvent.OnConversationClick(conversation.id)) },
                    onActionsToggle = { onEvent(ChatUiEvent.OnConversationActionsToggle(conversation.id)) },
                    onRenameClick = { onEvent(ChatUiEvent.OnRenameConversationClick(conversation.id)) },
                    onRenameDraftChange = { title -> onEvent(ChatUiEvent.OnRenameDraftChanged(title)) },
                    onRenameConfirm = { onEvent(ChatUiEvent.OnRenameConfirm) },
                    onRenameCancel = { onEvent(ChatUiEvent.OnRenameCancel) },
                    onDeleteClick = { onEvent(ChatUiEvent.OnDeleteConversationClick(conversation.id)) },
                    onDeleteConfirm = { onEvent(ChatUiEvent.OnDeleteConfirm) },
                    onDeleteCancel = { onEvent(ChatUiEvent.OnDeleteCancel) },
                )
            }
        }
    }
}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Rounded.SearchOff,
            contentDescription = null,
            modifier = Modifier.width(emptyStateIconSize),
            tint = MaterialTheme.colorScheme.outline,
        )
        VerticalSpacer(8)
        Text(
            text = stringResource(Res.string.chat_no_conversations_found),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun ChatConversationBucket.label(): String = when (this) {
    ChatConversationBucket.Today -> stringResource(Res.string.chat_bucket_today)
    ChatConversationBucket.Yesterday -> stringResource(Res.string.chat_bucket_yesterday)
    ChatConversationBucket.LastSevenDays -> stringResource(Res.string.chat_bucket_last_seven_days)
    is ChatConversationBucket.InMonth -> stringResource(month.toStringResource())
}
