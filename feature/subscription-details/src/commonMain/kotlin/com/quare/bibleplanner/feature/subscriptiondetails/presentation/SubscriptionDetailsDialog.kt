package com.quare.bibleplanner.feature.subscriptiondetails.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.subscription_details.generated.resources.Res
import bibleplanner.feature.subscription_details.generated.resources.active_status
import bibleplanner.feature.subscription_details.generated.resources.expires_label
import bibleplanner.feature.subscription_details.generated.resources.origin_label
import bibleplanner.feature.subscription_details.generated.resources.plan_label
import bibleplanner.feature.subscription_details.generated.resources.plan_value_pro
import bibleplanner.feature.subscription_details.generated.resources.purchase_date_label
import bibleplanner.feature.subscription_details.generated.resources.purchase_details_title
import bibleplanner.feature.subscription_details.generated.resources.renews_label
import bibleplanner.feature.subscription_details.generated.resources.store_name_ios
import bibleplanner.feature.subscription_details.generated.resources.subscription_status_title
import com.quare.bibleplanner.feature.subscriptiondetails.presentation.model.SubscriptionDetailsUiState
import com.quare.bibleplanner.feature.subscriptiondetails.presentation.viewmodel.SubscriptionDetailsViewModel
import com.quare.bibleplanner.ui.theme.success
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SubscriptionDetailsDialog(
    onDismiss: () -> Unit,
    viewModel: SubscriptionDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    if (state is SubscriptionDetailsUiState.Loaded) {
        val loadedState = state as SubscriptionDetailsUiState.Loaded
        AlertDialog(
            onDismissRequest = onDismiss,
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Subscription Status
                    SectionHeader(stringResource(Res.string.subscription_status_title))
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource(Res.string.active_status),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.success,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    ListItem(
                        headlineContent = {
                            Text(
                                text = loadedState.planName ?: stringResource(Res.string.plan_value_pro),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        overlineContent = {
                            Text(
                                text = stringResource(Res.string.plan_label),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Purchase Details
                    SectionHeader(stringResource(Res.string.purchase_details_title))

                    loadedState.purchaseDate?.let { date ->
                        SubscriptionDetailItem(
                            icon = Icons.Filled.CalendarToday,
                            headline = stringResource(Res.string.purchase_date_label),
                            supporting = date.format(),
                        )
                    }

                    loadedState.expirationDate?.let { date ->
                        val labelRes = if (loadedState.willRenew) {
                            Res.string.renews_label
                        } else {
                            Res.string.expires_label
                        }
                        SubscriptionDetailItem(
                            icon = Icons.Filled.CalendarToday,
                            headline = stringResource(labelRes),
                            supporting = date.format(),
                        )
                    }

                    SubscriptionDetailItem(
                        icon = Icons.Filled.Store,
                        headline = stringResource(Res.string.origin_label),
                        supporting = stringResource(Res.string.store_name_ios),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SubscriptionDetailItem(
    icon: ImageVector,
    headline: String,
    supporting: String,
) {
    ListItem(
        headlineContent = {
            Text(
                text = headline,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        },
        supportingContent = {
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun LocalDateTime.format(): String {
    val monthStr = stringResource(month.toStringResource()).lowercase().replaceFirstChar { it.uppercase() }.take(3)
    val minuteStr = minute.toString().padStart(2, '0')
    val hourStr = hour.toString().padStart(2, '0')
    return "$monthStr $day, $year · $hourStr:$minuteStr"
}
