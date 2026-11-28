package com.quare.bibleplanner.android.feature.read

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningFinishOfferUiModel

private const val BORDER_ALPHA = 0.2f
private val cardMaxWidth = 520.dp
private val cardCornerRadius = 18.dp
private val actionHeight = 36.dp

@Composable
internal fun ListeningFinishOfferCard(
    offer: ListeningFinishOfferUiModel,
    onMarkReadClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .widthIn(max = cardMaxWidth)
            .fillMaxWidth(),
        shape = RoundedCornerShape(cardCornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = BORDER_ALPHA),
        ),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(
                start = 14.dp,
                top = 12.dp,
                bottom = 12.dp,
                end = 4.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.read_listening_finish_offer_title, chapterTitle(offer.chapter)),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = offer.playingChapter?.let {
                        stringResource(R.string.read_listening_finish_offer_continuing, chapterTitle(it))
                    } ?: stringResource(R.string.read_listening_finish_offer_done),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                modifier = Modifier.height(actionHeight),
                onClick = onMarkReadClick,
                contentPadding = PaddingValues(horizontal = 14.dp),
            ) {
                Text(text = stringResource(R.string.read_mark_as_read))
            }
            IconButton(onClick = onDismissClick) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.read_listening_dismiss),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
