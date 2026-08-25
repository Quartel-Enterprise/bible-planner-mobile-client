package com.quare.bibleplanner.feature.chat.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chat.generated.resources.Res
import bibleplanner.feature.chat.generated.resources.chat_cooldown_input
import bibleplanner.feature.chat.generated.resources.chat_free_questions_left
import bibleplanner.feature.chat.generated.resources.chat_input_placeholder_generic
import bibleplanner.feature.chat.generated.resources.chat_input_placeholder_with_context
import bibleplanner.feature.chat.generated.resources.chat_locked_input
import bibleplanner.feature.chat.generated.resources.chat_send
import com.quare.bibleplanner.feature.chat.presentation.model.ChatInputMode
import com.quare.bibleplanner.feature.chat.presentation.model.ChatQuotaUiModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val fieldShape = CircleShape
private val sendButtonSize = 48.dp
private val lockedBarHeight = 52.dp
private val inputMaxHeight = 120.dp
private val quotaIconSize = 16.dp

@Composable
internal fun ChatInputBar(
    input: String,
    mode: ChatInputMode,
    cooldownSeconds: Int,
    contextLabel: String?,
    quota: ChatQuotaUiModel?,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onSubscribeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        when (mode) {
            ChatInputMode.LOCKED -> LockedBar(onSubscribeClick)

            ChatInputMode.COOLDOWN -> CooldownBar(cooldownSeconds)

            ChatInputMode.ENABLED -> ComposerRow(
                input = input,
                contextLabel = contextLabel,
                onInputChange = onInputChange,
                onSendClick = onSendClick,
            )
        }
        if (quota != null && mode != ChatInputMode.LOCKED) QuotaFooter(quota)
    }
}

@Composable
private fun ComposerRow(
    input: String,
    contextLabel: String?,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier
                .weight(1f)
                .heightIn(max = inputMaxHeight),
            placeholder = {
                Text(
                    text = contextLabel
                        ?.let { stringResource(Res.string.chat_input_placeholder_with_context, it) }
                        ?: stringResource(Res.string.chat_input_placeholder_generic),
                )
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            shape = fieldShape,
        )
        FilledIconButton(
            onClick = onSendClick,
            modifier = Modifier.size(sendButtonSize),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = stringResource(Res.string.chat_send),
            )
        }
    }
}

@Composable
private fun LockedBar(onSubscribeClick: () -> Unit) {
    Button(
        onClick = onSubscribeClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = lockedBarHeight),
    ) {
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            modifier = Modifier.size(quotaIconSize),
        )
        Text(
            text = stringResource(Res.string.chat_locked_input),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun CooldownBar(cooldownSeconds: Int) {
    Button(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = lockedBarHeight),
        enabled = false,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Icon(
            imageVector = Icons.Rounded.HourglassTop,
            contentDescription = null,
            modifier = Modifier.size(quotaIconSize),
        )
        Text(
            text = stringResource(Res.string.chat_cooldown_input, cooldownSeconds),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun QuotaFooter(quota: ChatQuotaUiModel) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Bolt,
            contentDescription = null,
            modifier = Modifier.size(quotaIconSize),
            tint = MaterialTheme.colorScheme.tertiary,
        )
        Text(
            text = pluralStringResource(
                Res.plurals.chat_free_questions_left,
                quota.remainingFree,
                quota.remainingFree,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}
