package com.quare.bibleplanner.feature.contactsupport.presentation.factory

import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiState

internal fun interface BuildSupportEmailBody {
    suspend operator fun invoke(state: ContactSupportUiState): String
}
