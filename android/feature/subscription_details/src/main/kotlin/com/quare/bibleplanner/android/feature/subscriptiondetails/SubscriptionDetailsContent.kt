package com.quare.bibleplanner.android.feature.subscriptiondetails

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Shop
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.date.rememberDateTimeLabel
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.android.designsystem.theme.success
import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType
import com.quare.bibleplanner.core.provider.billing.domain.model.PurchaseStore
import com.quare.bibleplanner.feature.subscriptiondetails.presentation.model.SubscriptionDetailsUiState

@Composable
internal fun SubscriptionDetailsContent(
    state: SubscriptionDetailsUiState.Loaded,
    onClose: () -> Unit,
) {
    AppAlertDialog(
        title = stringResource(R.string.subscription_details_status_title),
        confirmText = stringResource(R.string.subscription_details_close),
        onConfirm = onClose,
        dismissText = null,
        onDismiss = onClose,
        icon = Icons.Outlined.WorkspacePremium,
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            DetailItem(
                icon = Icons.Rounded.CheckCircle,
                iconTint = MaterialTheme.colorScheme.success,
                headline = stringResource(R.string.subscription_details_active),
                supporting = stringResource(state.planType.toLabel()),
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = stringResource(R.string.subscription_details_purchase_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            state.purchaseDate?.let { date ->
                DetailItem(
                    icon = Icons.Outlined.CalendarToday,
                    headline = stringResource(R.string.subscription_details_purchase_date),
                    supporting = rememberDateTimeLabel(date),
                )
            }
            state.expirationDate?.let { date ->
                DetailItem(
                    icon = Icons.Outlined.CalendarToday,
                    headline = stringResource(
                        if (state.willRenew) {
                            R.string.subscription_details_renews
                        } else {
                            R.string.subscription_details_expires
                        },
                    ),
                    supporting = rememberDateTimeLabel(date),
                )
            }
            state.store?.let { store ->
                DetailItem(
                    icon = Icons.Outlined.Shop,
                    headline = stringResource(R.string.subscription_details_origin),
                    supporting = stringResource(store.toLabel()),
                )
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    headline: String,
    supporting: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    ListItem(
        headlineContent = { Text(text = headline) },
        supportingContent = { Text(text = supporting) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@StringRes
private fun ProPlanType.toLabel(): Int = when (this) {
    ProPlanType.MONTHLY -> R.string.subscription_details_plan_pro_monthly
    ProPlanType.ANNUAL -> R.string.subscription_details_plan_pro_annual
    ProPlanType.UNKNOWN -> R.string.subscription_details_plan_pro
}

@StringRes
private fun PurchaseStore.toLabel(): Int = when (this) {
    PurchaseStore.PLAY_STORE -> R.string.subscription_details_store_google_play
    PurchaseStore.APP_STORE -> R.string.subscription_details_store_app_store
    PurchaseStore.WEB -> R.string.subscription_details_store_web
}
