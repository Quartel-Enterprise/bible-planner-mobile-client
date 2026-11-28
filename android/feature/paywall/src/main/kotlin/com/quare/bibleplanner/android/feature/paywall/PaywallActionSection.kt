package com.quare.bibleplanner.android.feature.paywall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState

private const val PRICE_ALPHA = 0.75f
private const val RESTORE_LINK_TAG = "restore"

@Composable
internal fun PaywallActionSection(
    uiState: PaywallUiState,
    buttonHeight: Dp,
    onStartProClick: () -> Unit,
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is PaywallUiState.Success -> Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StartProButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),
                isLoading = uiState.isPurchasing,
                priceDescription = selectedPlanPriceText(uiState),
                onClick = onStartProClick,
            )
            Spacer(modifier = Modifier.height(12.dp))
            PaywallLegalFooter(
                storeName = uiState.storeName,
                onRestoreClick = onRestoreClick,
            )
        }

        PaywallUiState.Loading -> PaywallLoadingIndicator(modifier = modifier)

        PaywallUiState.Error -> PaywallErrorCard(modifier = modifier)
    }
}

@Composable
internal fun StartProButton(
    isLoading: Boolean,
    priceDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = stringResource(R.string.paywall_subscribe),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (priceDescription != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    modifier = Modifier.weight(
                        weight = 1f,
                        fill = false,
                    ),
                    text = "· $priceDescription",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = LocalContentColor.current.copy(alpha = PRICE_ALPHA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
internal fun PaywallLegalFooter(
    storeName: String,
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RestorePurchaseText(onClick = onRestoreClick)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(14.dp),
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = stringResource(R.string.paywall_footer_payment_info, storeName),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun PaywallErrorCard(modifier: Modifier = Modifier) {
    val errorColor = MaterialTheme.colorScheme.error
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = errorColor,
        ),
    ) {
        Text(
            text = stringResource(R.string.paywall_error_message),
            modifier = Modifier.padding(16.dp),
            color = errorColor,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PaywallLoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator()
    }
}

@Composable
internal fun selectedPlanPriceText(uiState: PaywallUiState.Success): String? {
    val selectedPlan = uiState.subscriptionPlans.firstOrNull { plan -> plan.isSelected } ?: return null
    return "${selectedPlan.priceDescription}/${stringResource(selectedPlan.type.toPeriodUnit())}"
}

@Composable
private fun RestorePurchaseText(onClick: () -> Unit) {
    val question = stringResource(R.string.paywall_already_subscribed)
    val action = stringResource(R.string.paywall_restore_purchase)
    val linkStyles = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        ),
    )
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                append("$question ")
            }
            withLink(
                LinkAnnotation.Clickable(
                    tag = RESTORE_LINK_TAG,
                    styles = linkStyles,
                    linkInteractionListener = { onClick() },
                ),
            ) {
                append(action)
            }
        },
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
    )
}
