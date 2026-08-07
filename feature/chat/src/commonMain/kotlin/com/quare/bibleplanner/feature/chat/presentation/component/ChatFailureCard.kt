package com.quare.bibleplanner.feature.chat.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chat.generated.resources.Res
import bibleplanner.feature.chat.generated.resources.chat_offline_message
import bibleplanner.feature.chat.generated.resources.chat_offline_title
import bibleplanner.feature.chat.generated.resources.chat_rate_limit_countdown
import bibleplanner.feature.chat.generated.resources.chat_rate_limit_message
import bibleplanner.feature.chat.generated.resources.chat_rate_limit_title
import bibleplanner.feature.chat.generated.resources.chat_retry
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendFailureModel
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

private val cardShape = RoundedCornerShape(
    topStart = 4.dp,
    topEnd = 18.dp,
    bottomEnd = 18.dp,
    bottomStart = 18.dp,
)
private val cardMaxWidthFraction = 0.9f
private val failureIconSize = 20.dp

@Composable
internal fun ChatFailureCard(
    failure: ChatSendFailureModel,
    cooldownSeconds: Int,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (failure is ChatSendFailureModel.LimitReached) return
    val isRateLimited = failure is ChatSendFailureModel.RateLimited
    Surface(
        modifier = modifier.fillMaxWidth(cardMaxWidthFraction),
        shape = cardShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (isRateLimited) Icons.Rounded.Schedule else Icons.Rounded.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(failureIconSize),
                    tint = if (isRateLimited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
                Text(
                    text = stringResource(
                        if (isRateLimited) Res.string.chat_rate_limit_title else Res.string.chat_offline_title,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            VerticalSpacer(6)
            Text(
                text = stringResource(
                    if (isRateLimited) Res.string.chat_rate_limit_message else Res.string.chat_offline_message,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalSpacer(10)
            if (isRateLimited && cooldownSeconds > 0) {
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(Res.string.chat_rate_limit_countdown, cooldownSeconds)) },
                    enabled = false,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.HourglassTop,
                            contentDescription = null,
                            modifier = Modifier.size(failureIconSize),
                            tint = Color.Unspecified,
                        )
                    },
                )
            } else {
                Button(onClick = onRetryClick) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(failureIconSize),
                    )
                    Text(
                        text = stringResource(Res.string.chat_retry),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}
