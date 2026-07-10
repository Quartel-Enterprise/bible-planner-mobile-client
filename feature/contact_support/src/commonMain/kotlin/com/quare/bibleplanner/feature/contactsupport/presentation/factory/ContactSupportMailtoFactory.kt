package com.quare.bibleplanner.feature.contactsupport.presentation.factory

import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiState

internal fun interface ContactSupportMailtoFactory {
    suspend fun create(state: ContactSupportUiState): String
}
