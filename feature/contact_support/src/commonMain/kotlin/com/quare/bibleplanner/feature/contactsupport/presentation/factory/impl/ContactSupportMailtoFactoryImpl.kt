package com.quare.bibleplanner.feature.contactsupport.presentation.factory.impl

import bibleplanner.feature.contact_support.generated.resources.Res
import bibleplanner.feature.contact_support.generated.resources.contact_support_email_subject
import com.quare.bibleplanner.feature.contactsupport.domain.model.SupportContact
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.BuildSupportEmailBody
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.ContactSupportMailtoFactory
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.EncodeMailtoComponent
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiState
import org.jetbrains.compose.resources.getString

internal class ContactSupportMailtoFactoryImpl(
    private val buildSupportEmailBody: BuildSupportEmailBody,
    private val encodeMailtoComponent: EncodeMailtoComponent,
) : ContactSupportMailtoFactory {
    override suspend fun create(state: ContactSupportUiState): String {
        val subject = getString(Res.string.contact_support_email_subject)
        val body = buildSupportEmailBody(state)
        return "mailto:${SupportContact.EMAIL}" +
            "?subject=${encodeMailtoComponent(subject)}" +
            "&body=${encodeMailtoComponent(body)}"
    }
}
