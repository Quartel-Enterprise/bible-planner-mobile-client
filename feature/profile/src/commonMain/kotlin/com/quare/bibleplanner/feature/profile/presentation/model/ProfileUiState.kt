package com.quare.bibleplanner.feature.profile.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.profile.domain.model.AccountStatusModel
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

internal data class ProfileUiState(
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
    val currentDate: LocalDate,
    val appVersion: String,
    val isUpdateRowVisible: Boolean,
    val isCheckingForUpdate: Boolean,
)
