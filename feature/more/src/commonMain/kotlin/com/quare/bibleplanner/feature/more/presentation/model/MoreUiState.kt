package com.quare.bibleplanner.feature.more.presentation.model

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.feature.more.domain.model.AccountStatusModel
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

internal sealed interface MoreUiState {
    data object Loading : MoreUiState

    data class Loaded(
        val themeRes: StringResource,
        val contrastRes: StringResource?,
        val planStartDate: LocalDate?,
        val currentDate: LocalDate,
        val subscriptionStatus: SubscriptionStatus?,
        val isInstagramLinkVisible: Boolean,
        val shouldShowDonateOption: Boolean,
        val headerRes: StringResource?,
        val showSubscriptionDetailsDialog: Boolean = false,
        val isProCardVisible: Boolean,
        val isWebAppVisible: Boolean,
        val appVersion: String,
        val isLoginVisible: Boolean,
        val accountStatusModel: AccountStatusModel,
        val bibleVersionName: String?,
        val bibleDownloadProgress: Float?,
    ) : MoreUiState
}
