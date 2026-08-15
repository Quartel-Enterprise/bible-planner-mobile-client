package com.quare.bibleplanner.feature.shareverse.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import bibleplanner.feature.share_verse.generated.resources.Res
import bibleplanner.feature.share_verse.generated.resources.share_as_image
import bibleplanner.feature.share_verse.generated.resources.share_as_image_description
import bibleplanner.feature.share_verse.generated.resources.share_as_text
import bibleplanner.feature.share_verse.generated.resources.share_as_text_description
import com.quare.bibleplanner.feature.shareverse.presentation.model.ShareVerseUiEvent
import com.quare.bibleplanner.feature.shareverse.presentation.model.ShareVerseUiState
import org.jetbrains.compose.resources.stringResource

private val optionIconSize = 44.dp

@Composable
internal fun ShareVerseContent(
    uiState: ShareVerseUiState,
    onEvent: (ShareVerseUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = uiState.reference,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        ShareOption(
            imageVector = Icons.Default.Subject,
            title = stringResource(Res.string.share_as_text),
            description = stringResource(Res.string.share_as_text_description),
            isEnabled = uiState.isReady,
            onClick = { onEvent(ShareVerseUiEvent.OnShareAsTextClick) },
        )
        ShareOption(
            imageVector = Icons.Default.Image,
            title = stringResource(Res.string.share_as_image),
            description = stringResource(Res.string.share_as_image_description),
            isEnabled = uiState.isReady,
            onClick = { onEvent(ShareVerseUiEvent.OnShareAsImageClick) },
        )
    }
}

@Composable
private fun ShareOption(
    imageVector: ImageVector,
    title: String,
    description: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = isEnabled, onClick = onClick)
                .padding(12.dp),
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
