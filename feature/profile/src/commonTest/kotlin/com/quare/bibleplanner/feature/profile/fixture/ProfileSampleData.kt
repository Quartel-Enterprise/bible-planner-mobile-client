package com.quare.bibleplanner.feature.profile.fixture

import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.theme_system
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.profile.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel
import kotlinx.datetime.LocalDate

internal const val SAMPLE_BIBLE_VERSION_NAME = "King James Version"

internal val samplePlanStartDate = LocalDate(
    year = 2026,
    month = 1,
    day = 5,
)

internal val sampleUserProfile = UserProfile(
    userId = "user-1",
    displayName = "Ana Souza",
    email = "ana@example.com",
    avatar = AvatarSource.None,
    hasVisiblePhoto = false,
    hasProviderPhoto = false,
    isUsingProviderPhoto = false,
    provider = null,
)

internal fun profileUiState(accountStatusModel: AccountStatusModel): ProfileUiState = ProfileUiState(
    accountStatusModel = accountStatusModel,
    subscriptionStatus = Loadable.Loaded(SubscriptionStatus.Free),
    isProCardVisible = Loadable.Loaded(false),
    shouldShowDonateOption = Loadable.Loaded(false),
    headerRes = Loadable.Loaded(null),
    isInstagramLinkVisible = Loadable.Loaded(false),
    isWebAppVisible = Loadable.Loaded(false),
    themeRes = Loadable.Loaded(Res.string.theme_system),
    contrastRes = Loadable.Loaded(null),
    selectedLanguage = Loadable.Loaded(Language.ENGLISH),
    bibleVersionName = Loadable.Loaded(SAMPLE_BIBLE_VERSION_NAME),
    bibleDownloadProgress = Loadable.Loaded(null),
    planStartDate = Loadable.Loaded(samplePlanStartDate),
    studySuggestion = Loadable.Loaded(
        StudySuggestionSettingsModel(
            isEnabled = true,
            mode = StudySuggestionMode.DIALOG,
        ),
    ),
    currentDate = LocalDate(
        year = 2026,
        month = 9,
        day = 26,
    ),
    appVersion = "3.0.0",
    isUpdateRowVisible = false,
    isCheckingForUpdate = false,
)
