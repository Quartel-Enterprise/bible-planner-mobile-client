package com.quare.bibleplanner.feature.contactsupport.presentation.factory.impl

import bibleplanner.feature.contact_support.generated.resources.Res
import bibleplanner.feature.contact_support.generated.resources.contact_support_email_subject
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.contactsupport.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.contactsupport.domain.model.SupportContact
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiState
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ContactSupportMailtoFactoryImplTest {
    private lateinit var mailtoFactory: ContactSupportMailtoFactoryImpl

    @Test
    fun `GIVEN a body and subject WHEN creating the mailto link THEN assembles the encoded subject and body`() =
        runTest {
            // Given
            prepareScenario(
                body = "line one\nline two",
                encode = { value -> value.replace(" ", "_").replace("\n", "|") },
            )

            // When
            val mailto = mailtoFactory.create(someState())

            // Then
            val expectedSubject = getString(Res.string.contact_support_email_subject).replace(" ", "_")
            assertEquals(
                "mailto:${SupportContact.EMAIL}?subject=$expectedSubject&body=line_one|line_two",
                mailto,
            )
        }

    @Test
    fun `GIVEN a ui state WHEN creating the mailto link THEN passes it through to the body builder unchanged`() =
        runTest {
            // Given
            var receivedState: ContactSupportUiState? = null
            prepareScenario(
                buildBody = { state ->
                    receivedState = state
                    "body"
                },
            )
            val state = someState()

            // When
            mailtoFactory.create(state)

            // Then
            assertEquals(state, receivedState)
        }

    private fun someState(): ContactSupportUiState = ContactSupportUiState(
        accountStatusModel = AccountStatusModel.LoggedOut,
        subscriptionStatus = Loadable.Loaded(SubscriptionStatus.Free),
        selectedLanguage = Loadable.Loaded(Language.ENGLISH),
        appVersion = "1.0.0",
        platform = Platform.Android,
    )

    private fun prepareScenario(
        body: String = "body",
        buildBody: suspend (ContactSupportUiState) -> String = { body },
        encode: (String) -> String = { it },
    ) {
        mailtoFactory = ContactSupportMailtoFactoryImpl(
            buildSupportEmailBody = { state -> buildBody(state) },
            encodeMailtoComponent = { value -> encode(value) },
        )
    }
}
