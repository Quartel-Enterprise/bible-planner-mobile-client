package com.quare.bibleplanner.feature.profile.presentation.model

import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.profile.domain.model.AccountStatusModel
import kotlinx.datetime.LocalDate
import software.quare.loadable.Loadable

data class ProfileUiState(
    val accountStatusModel: AccountStatusModel,
    val subscriptionStatus: Loadable<SubscriptionStatus?>,
    val isProCardVisible: Loadable<Boolean>,
    val shouldShowDonateOption: Loadable<Boolean>,
    val isInstagramLinkVisible: Loadable<Boolean>,
    val theme: Loadable<Theme>,
    val colorScheme: Loadable<ProfileColorScheme>,
    val selectedLanguage: Loadable<Language>,
    val bibleVersionName: Loadable<String?>,
    val bibleDownloadProgress: Loadable<Float?>,
    val planStartDate: Loadable<LocalDate?>,
    val studySuggestion: Loadable<StudySuggestionSettingsModel>,
    val annotationsSummary: Loadable<AnnotationsSummaryModel>,
    val currentDate: LocalDate,
    val appVersion: String,
    val isUpdateRowVisible: Boolean,
    val isCheckingForUpdate: Boolean,
    val isPrivacyOptionsVisible: Boolean,
)
