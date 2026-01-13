package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.become_pro_subtitle
import bibleplanner.feature.more.generated.resources.donate_option
import bibleplanner.feature.more.generated.resources.donate_subtitle
import bibleplanner.feature.more.generated.resources.pro_description_simple
import bibleplanner.feature.more.generated.resources.you_re_pro
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.ui.component.ResponsiveContentScope
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.theme.gold
import org.jetbrains.compose.resources.stringResource

internal fun ResponsiveContentScope.headerSection(
    state: MoreUiState.Loaded,
    onEvent: (MoreUiEvent) -> Unit,
    becomeProTitleContent: @Composable () -> Unit,
) {
    state.headerRes?.let { headerRes ->
        responsiveItem {
            SectionHeaderText(title = stringResource(headerRes))
        }
        item { VerticalSpacer() }
    }
    responsiveItem {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.isProCardVisible) {
                when (state.subscriptionStatus) {
                    SubscriptionStatus.Free -> {
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            titleContent = becomeProTitleContent,
                            subtitle = stringResource(Res.string.become_pro_subtitle),
                            icon = Icons.Default.Star,
                            onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.BECOME_PRO)) },
                        )
                    }

                    is SubscriptionStatus.Pro -> {
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            titleContent = {
                                Text(
                                    text = stringResource(Res.string.you_re_pro),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            },
                            subtitle = stringResource(Res.string.pro_description_simple),
                            icon = Icons.Default.Star,
                            iconTint = MaterialTheme.gold,
                            border = BorderStroke(2.dp, MaterialTheme.gold),
                            onClick = { onEvent(MoreUiEvent.OnProCardClick) },
                        )
                    }

                    null -> {
                        Unit
                    }
                }
            }
            if (state.shouldShowDonateOption) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    titleContent = {
                        Text(
                            text = stringResource(Res.string.donate_option),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    subtitle = stringResource(Res.string.donate_subtitle),
                    icon = Icons.Default.Favorite,
                    onClick = { onEvent(MoreUiEvent.OnItemClick(MoreOptionItemType.DONATE)) },
                )
            }
        }
    }
}
