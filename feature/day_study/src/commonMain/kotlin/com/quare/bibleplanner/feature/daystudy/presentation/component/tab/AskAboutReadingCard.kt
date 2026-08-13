package com.quare.bibleplanner.feature.daystudy.presentation.component.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_chat_entry_hint
import bibleplanner.feature.day_study.generated.resources.ai_chat_entry_placeholder
import bibleplanner.feature.day_study.generated.resources.ai_chat_entry_title
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

private val cardShape = RoundedCornerShape(16.dp)
private val fieldShape = RoundedCornerShape(12.dp)
private val iconSize = 20.dp
private val hintIconSize = 16.dp

@Composable
internal fun AskAboutReadingCard(
    passageLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        color = MaterialTheme.colorScheme.primaryContainer,
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
                    imageVector = Icons.AutoMirrored.Rounded.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = stringResource(Res.string.ai_chat_entry_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            VerticalSpacer(10)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = fieldShape,
                    ).padding(
                        horizontal = 14.dp,
                        vertical = 11.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.ai_chat_entry_placeholder, passageLabel),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            VerticalSpacer(8)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(hintIconSize),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = stringResource(Res.string.ai_chat_entry_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
