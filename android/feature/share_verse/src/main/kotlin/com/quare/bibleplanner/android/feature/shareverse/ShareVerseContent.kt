package com.quare.bibleplanner.android.feature.shareverse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.sheet.AppModalBottomSheet
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiEvent
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiState

private val optionIconSize = 44.dp

@Composable
internal fun ShareVerseContent(
    uiState: ShareVerseUiState,
    actionGuard: ShareActionGuard,
    onEvent: (ShareVerseUiEvent) -> Unit,
) {
    AppModalBottomSheet(
        title = stringResource(R.string.share_verse_title),
        subtitle = uiState.reference.takeIf { it.isNotEmpty() },
        onDismiss = { onEvent(ShareVerseUiEvent.OnDismiss) },
    ) {
        ShareActionGuardEffect(guard = actionGuard)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShareOptionItem(
                imageVector = Icons.AutoMirrored.Filled.Subject,
                title = stringResource(R.string.share_verse_as_text),
                description = stringResource(R.string.share_verse_as_text_description),
                isEnabled = uiState.isReady,
                onClick = { onEvent(ShareVerseUiEvent.OnShareAsTextClick) },
            )
            ShareOptionItem(
                imageVector = Icons.Default.Image,
                title = stringResource(R.string.share_verse_as_image),
                description = stringResource(R.string.share_verse_as_image_description),
                isEnabled = uiState.isReady,
                onClick = { onEvent(ShareVerseUiEvent.OnShareAsImageClick) },
            )
        }
    }
}

@Composable
private fun ShareOptionItem(
    imageVector: ImageVector,
    title: String,
    description: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(optionIconSize),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    modifier = Modifier.padding(10.dp),
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
