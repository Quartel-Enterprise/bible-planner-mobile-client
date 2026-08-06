package com.quare.bibleplanner.feature.chat.presentation.component.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chat.generated.resources.Res
import bibleplanner.feature.chat.generated.resources.chat_bucket_last_seven_days
import bibleplanner.feature.chat.generated.resources.chat_bucket_today
import bibleplanner.feature.chat.generated.resources.chat_bucket_yesterday
import bibleplanner.feature.chat.generated.resources.chat_conversations
import bibleplanner.feature.chat.generated.resources.chat_new_conversation
import bibleplanner.feature.chat.generated.resources.chat_no_conversations_found
import bibleplanner.feature.chat.generated.resources.chat_search_conversations
import com.quare.bibleplanner.feature.chat.presentation.model.ChatConversationBucket
import com.quare.bibleplanner.feature.chat.presentation.model.ChatHistoryUiState
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiEvent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.utils.toStringResource
import org.jetbrains.compose.resources.stringResource

private val drawerWidth = 320.dp
private val emptyStateIconSize = 36.dp
private const val SCRIM_ALPHA = 0.45f

@Composable
internal fun BoxScope.ChatHistoryDrawer(
    history: ChatHistoryUiState,
    onEvent: (ChatUiEvent) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    AnimatedVisibility(
        visible = history.isOpen,
        modifier = Modifier.matchParentSize(),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onEvent(ChatUiEvent.OnHistoryDismiss) },
                ),
        )
    }
    AnimatedVisibility(
        visible = history.isOpen,
        modifier = Modifier.align(Alignment.CenterEnd),
        enter = slideInHorizontally { width -> width },
        exit = slideOutHorizontally { width -> width },
    ) {
        Surface(
            modifier = Modifier
                .width(drawerWidth)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
        ) {
            Box(modifier = Modifier.systemBarsPadding()) {
                HistoryContent(
                    history = history,
                    onEvent = onEvent,
                )
                ExtendedFloatingActionButton(
                    onClick = { onEvent(ChatUiEvent.OnNewConversationClick) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                        )
                    },
                    text = { Text(stringResource(Res.string.chat_new_conversation)) },
                )
            }
        }
    }
}

@Composable
private fun HistoryContent(
    history: ChatHistoryUiState,
    onEvent: (ChatUiEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 8.dp,
                    top = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.chat_conversations),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = { onEvent(ChatUiEvent.OnHistoryDismiss) }) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(Res.string.chat_conversations),
                )
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
            )
        }
    }
}

@Composable
private fun ConversationList(
    history: ChatHistoryUiState,
    onEvent: (ChatUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            bottom = 96.dp,
        ),
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
