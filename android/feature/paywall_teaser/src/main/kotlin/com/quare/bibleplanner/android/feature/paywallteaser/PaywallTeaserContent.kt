package com.quare.bibleplanner.android.feature.paywallteaser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.icon.SheetIconTile
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.feature.paywallteaser.presentation.model.PaywallTeaserUiEvent
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard

private val bodyMaxWidth = 300.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PaywallTeaserContent(
    reason: PaywallTeaserReason,
    onEvent: (PaywallTeaserUiEvent) -> Unit,
) {
    val copy = reason.toCopy()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCloseGuard = rememberSheetCloseGuard()
    val onDismiss = { sheetCloseGuard.close { onEvent(PaywallTeaserUiEvent.OnDismiss) } }
    SheetExitAnimationEffect(
        animateOut = sheetState::hide,
        animateBackIn = sheetState::show,
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = !sheetCloseGuard.isClosing,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .blockPointerInput(isBlocked = sheetCloseGuard.isClosing)
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 16.dp,
                ).verticalScroll(rememberScrollState()),
        ) {
            SheetIconTile(imageVector = Icons.Rounded.WorkspacePremium)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(copy.title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                modifier = Modifier.widthIn(max = bodyMaxWidth),
                text = stringResource(copy.body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { sheetCloseGuard.close { onEvent(PaywallTeaserUiEvent.OnSubscribeClick) } },
            ) {
                Icon(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    imageVector = Icons.Rounded.WorkspacePremium,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = stringResource(R.string.paywall_teaser_subscribe))
            }
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(copy.dismiss))
            }
        }
    }
}
