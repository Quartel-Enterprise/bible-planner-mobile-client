package com.quare.bibleplanner.feature.paywallteaser.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.paywall_teaser.generated.resources.Res
import bibleplanner.feature.paywall_teaser.generated.resources.paywall_teaser_highlight_custom_color_body
import bibleplanner.feature.paywall_teaser.generated.resources.paywall_teaser_highlight_custom_color_dismiss
import bibleplanner.feature.paywall_teaser.generated.resources.paywall_teaser_highlight_custom_color_title
import bibleplanner.feature.paywall_teaser.generated.resources.paywall_teaser_subscribe
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.feature.paywallteaser.presentation.model.PaywallTeaserUiEvent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val iconBoxSize = 56.dp
private val iconBoxCornerRadius = 16.dp
private val iconSize = 28.dp
private val bodyMaxWidth = 300.dp

/**
 * A small "why Pro" step shown before the real paywall, reused by every feature that gates a
 * capability behind a subscription: the reason picks the copy, and confirming always lands on the
 * same [com.quare.bibleplanner.core.model.route.PaywallNavRoute].
 */
@Composable
internal fun PaywallTeaserSheet(
    reason: PaywallTeaserReason,
    onEvent: (PaywallTeaserUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = reason.toCopy()
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(iconBoxSize)
                .clip(RoundedCornerShape(iconBoxCornerRadius))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        VerticalSpacer(12)
        Text(
            text = stringResource(copy.title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(6)
        Text(
            modifier = Modifier.widthIn(max = bodyMaxWidth),
            text = stringResource(copy.body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(20)
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onEvent(PaywallTeaserUiEvent.OnSubscribeClick) },
        ) {
            Icon(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text(text = stringResource(Res.string.paywall_teaser_subscribe))
        }
        TextButton(
            onClick = { onEvent(PaywallTeaserUiEvent.OnDismiss) },
        ) {
            Text(text = stringResource(copy.dismiss))
        }
    }
}

private data class PaywallTeaserCopy(
    val title: StringResource,
    val body: StringResource,
    val dismiss: StringResource,
)

private fun PaywallTeaserReason.toCopy(): PaywallTeaserCopy = when (this) {
    PaywallTeaserReason.HIGHLIGHT_CUSTOM_COLOR -> PaywallTeaserCopy(
        title = Res.string.paywall_teaser_highlight_custom_color_title,
        body = Res.string.paywall_teaser_highlight_custom_color_body,
        dismiss = Res.string.paywall_teaser_highlight_custom_color_dismiss,
    )
}
