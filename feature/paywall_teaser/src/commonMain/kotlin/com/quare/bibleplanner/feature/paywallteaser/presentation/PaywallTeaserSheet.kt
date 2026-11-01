package com.quare.bibleplanner.feature.paywallteaser.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.paywall_teaser.generated.resources.Res
import bibleplanner.feature.paywall_teaser.generated.resources.paywall_teaser_subscribe
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.feature.paywallteaser.presentation.mapper.toCopy
import com.quare.bibleplanner.feature.paywallteaser.presentation.model.PaywallTeaserUiEvent
import com.quare.bibleplanner.ui.component.icon.SheetIconTile
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

private val bodyMaxWidth = 300.dp

@Composable
internal fun PaywallTeaserSheet(
    reason: PaywallTeaserReason,
    onEvent: (PaywallTeaserUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = reason.toCopy()
    Column(
        modifier = modifier.fillMaxWidth().padding(
            horizontal = 24.dp,
            vertical = 8.dp,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SheetIconTile(imageVector = Icons.Rounded.WorkspacePremium)
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
