package com.quare.bibleplanner.android.feature.chat

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.chat.presentation.model.ChatHistoryUiState
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiEvent

private val emptyStateIconSize = 36.dp
private val emptyHistoryIconSize = 40.dp
private val searchFieldShape = RoundedCornerShape(12.dp)
private val searchIconSize = 19.dp
private val clearIconSize = 20.dp

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
                        contentDescription = stringResource(R.string.chat_back),
                    )
                }
            }
            Text(
                text = stringResource(R.string.chat_conversations),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            onDismiss?.let { dismiss ->
                IconButton(onClick = dismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.chat_conversations),
                    )
                }
            }
        }
        ChatSearchField(
            query = history.query,
            onQueryChange = { query -> onEvent(ChatUiEvent.OnHistoryQueryChanged(query)) },
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (history.groups.isEmpty()) {
            if (history.hasConversations) ChatEmptySearchColumn() else ChatEmptyHistoryColumn()
        } else {
            ChatConversationList(
                history = history,
                onEvent = onEvent,
                contentPadding = contentPadding,
            )
        }
    }
}

@Composable
private fun ChatSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val synced = rememberSyncedTextFieldState(
        value = query,
        onValueChange = onQueryChange,
    )
    val isEmpty = synced.textFieldState.text.isEmpty()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = searchFieldShape,
            ).padding(
                horizontal = 12.dp,
                vertical = 9.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            modifier = Modifier.size(searchIconSize),
            tint = MaterialTheme.colorScheme.outline,
        )
        Box(modifier = Modifier.weight(1f)) {
            if (isEmpty) {
                Text(
                    text = stringResource(R.string.chat_search_conversations),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                state = synced.textFieldState,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                lineLimits = TextFieldLineLimits.SingleLine,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            )
        }
        if (!isEmpty) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = null,
                modifier = Modifier
                    .size(clearIconSize)
                    .clickable { synced.textFieldState.clearText() },
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun ChatConversationList(
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
                    text = bucketLabel(group.bucket),
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
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun ChatEmptyHistoryColumn() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 22.dp,
                vertical = 56.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Rounded.Forum,
            contentDescription = null,
            modifier = Modifier.size(emptyHistoryIconSize),
            tint = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = stringResource(R.string.chat_no_conversations_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.chat_no_conversations_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ChatEmptySearchColumn() {
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.chat_no_conversations_found),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}
