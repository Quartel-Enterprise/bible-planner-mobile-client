package com.quare.bibleplanner.feature.more.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.more.domain.model.AccountStatusModel
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

internal data class MoreUiState(
    val accountStatusModel: AccountStatusModel,
    val subscriptionStatus: Loadable<SubscriptionStatus?>,
    val isProCardVisible: Loadable<Boolean>,
    val shouldShowDonateOption: Loadable<Boolean>,
    val headerRes: Loadable<StringResource?>,
    val isInstagramLinkVisible: Loadable<Boolean>,
    val isWebAppVisible: Loadable<Boolean>,
    val themeRes: Loadable<StringResource>,
    val contrastRes: Loadable<StringResource?>,
    val selectedLanguage: Loadable<Language>,
    val bibleVersionName: Loadable<String?>,
    val bibleDownloadProgress: Loadable<Float?>,
    val planStartDate: Loadable<LocalDate?>,
    val showSubscriptionDetailsDialog: Boolean,
    val showContactSupportDialog: Boolean,
    val currentDate: LocalDate,
    val appVersion: String,
)
