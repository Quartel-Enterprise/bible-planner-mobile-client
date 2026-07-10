package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ElevatedCard
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
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.ui.component.ResponsiveContentScope
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.theme.gold
import org.jetbrains.compose.resources.stringResource

internal fun ResponsiveContentScope.headerSection(
    state: MoreUiState,
    onEvent: (MoreUiEvent) -> Unit,
    becomeProTitleContent: @Composable () -> Unit,
) {
    state.headerRes.valueOrNull()?.let { headerRes ->
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
            if (state.isProCardVisible.valueOrNull() == true) {
                when (val subscriptionStatus = state.subscriptionStatus) {
                    Loadable.Loading -> ProCardShimmer(modifier = Modifier.weight(1f))

                    is Loadable.Loaded -> {
                        when (subscriptionStatus.value) {
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

                            null -> {}
                        }
                    }
                }
            }
            if (state.shouldShowDonateOption.valueOrNull() == true) {
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

@Composable
private fun ProCardShimmer(modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ShimmerBox(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
            )
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(20.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(14.dp))
        }
    }
}
