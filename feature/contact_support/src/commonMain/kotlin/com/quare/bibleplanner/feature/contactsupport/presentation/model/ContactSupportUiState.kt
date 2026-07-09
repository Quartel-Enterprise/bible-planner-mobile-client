package com.quare.bibleplanner.feature.contactsupport.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.contactsupport.domain.model.AccountStatusModel

internal data class ContactSupportUiState(
    val accountStatusModel: AccountStatusModel,
    val subscriptionStatus: Loadable<SubscriptionStatus?>,
    val selectedLanguage: Loadable<Language>,
    val appVersion: String,
    val platform: Platform,
)
