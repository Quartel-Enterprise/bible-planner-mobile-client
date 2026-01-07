package com.quare.bibleplanner.feature.more.presentation.model

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

internal sealed interface MoreUiState {
    data object Loading : MoreUiState

    data class Loaded(
        val themeSubtitle: StringResource,
        val planStartDate: LocalDate?,
        val currentDate: LocalDate,
        val subscriptionStatus: SubscriptionStatus?,
        val isInstagramLinkVisible: Boolean,
        val shouldShowDonateOption: Boolean,
        val headerRes: StringResource?,
        val showSubscriptionDetailsDialog: Boolean = false,
        val isPremiumCardVisible: Boolean,
    ) : MoreUiState
}
