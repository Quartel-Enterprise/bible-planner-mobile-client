package com.quare.bibleplanner.android.feature.profile

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.theme.isDark
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import software.quare.loadable.Loadable
import software.quare.loadable.valueOrNull

// Why: the app's gold for Pro, which Material 3 has no role for.
private val goldLight = Color(0xFFDAA520)
private val goldDark = Color(0xFFFFD700)

private val ColorScheme.gold: Color
    get() = if (isDark()) goldDark else goldLight

@Composable
internal fun ProfileProAndDonateSection(
    state: ProfileUiState,
    transitionScopes: ProfileTransitionScopes?,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        state.shouldShowDonateOption.valueOrNull()?.let { shouldShowDonate ->
            ProfileSectionHeader(
                title = stringResource(
                    if (shouldShowDonate) R.string.profile_pro_and_support else R.string.profile_pro_section,
                ),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.isProCardVisible.valueOrNull() == true) {
                when (val subscriptionStatus = state.subscriptionStatus) {
                    Loadable.Loading -> ProCardSkeleton(modifier = Modifier.weight(1f))

                    is Loadable.Loaded -> when (subscriptionStatus.value) {
                        SubscriptionStatus.Free -> ProfileActionCard(
                            modifier = Modifier.weight(1f),
                            titleContent = { BecomeProTitle(transitionScopes = transitionScopes) },
                            subtitle = stringResource(R.string.profile_become_pro_subtitle),
                            icon = Icons.Default.Star,
                            onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.BECOME_PRO)) },
                        )

                        is SubscriptionStatus.Pro -> {
                            val gold = MaterialTheme.colorScheme.gold
                            ProfileActionCard(
                                modifier = Modifier.weight(1f),
                                titleContent = { ActionCardTitle(text = stringResource(R.string.profile_you_re_pro)) },
                                subtitle = stringResource(R.string.profile_pro_description_simple),
                                icon = Icons.Default.Star,
                                iconTint = gold,
                                border = BorderStroke(
                                    width = 2.dp,
                                    color = gold,
                                ),
                                onClick = { onEvent(ProfileUiEvent.OnProCardClick) },
                            )
                        }

                        null -> Unit
                    }
                }
            }
            if (state.shouldShowDonateOption.valueOrNull() == true) {
                ProfileActionCard(
                    modifier = Modifier.weight(1f),
                    titleContent = { ActionCardTitle(text = stringResource(R.string.profile_donate_option)) },
                    subtitle = stringResource(R.string.profile_donate_subtitle),
                    icon = Icons.Default.Favorite,
                    onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.DONATE)) },
                )
            }
        }
    }
}

@Composable
private fun BecomeProTitle(transitionScopes: ProfileTransitionScopes?) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        ActionCardTitle(
            text = stringResource(R.string.profile_become_pro_part_1),
            modifier = Modifier.sharedElementWithRelayoutOf(
                transitionScopes = transitionScopes,
                key = BECOME_PRO_PART_1_KEY,
            ),
        )
        ActionCardTitle(
            text = stringResource(R.string.profile_become_pro_part_2),
            modifier = Modifier.sharedElementWithRelayoutOf(
                transitionScopes = transitionScopes,
                key = BECOME_PRO_PART_2_KEY,
            ),
        )
    }
}

@Composable
private fun ActionCardTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun ProfileActionCard(
    titleContent: @Composable () -> Unit,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    border: BorderStroke? = null,
) {
    // Why: ElevatedCard takes no border, which the Pro card needs.
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(),
        elevation = CardDefaults.elevatedCardElevation(),
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
            )
            titleContent()
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProCardSkeleton(modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProfileSkeletonBox(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
            )
            ProfileSkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(20.dp))
            ProfileSkeletonBox(modifier = Modifier.fillMaxWidth().height(14.dp))
        }
    }
}
