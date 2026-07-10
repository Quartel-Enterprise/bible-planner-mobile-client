package com.quare.bibleplanner.feature.contactsupport.presentation.viewmodel

import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.user.data.mapper.SessionUserMapper
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.ContactSupportMailtoFactory
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.ContactSupportUiStateFactory
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiEvent
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiState
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class ContactSupportViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ContactSupportViewModel
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `GIVEN account and subscription loading WHEN sending support email THEN logs event without params`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                sessionStatus = SessionStatus.Initializing,
                subscriptionStatusFlow = emptyFlow(),
            )

            // When
            viewModel.onEvent(ContactSupportUiEvent.OnSendEmailClick)

            // Then
            assertEquals(
                listOf("contact_support_email_opened" to emptyMap<String, Any>()),
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN a logged out free user WHEN sending the support email THEN logs is_logged_in and is_pro as false`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                sessionStatus = SessionStatus.NotAuthenticated(),
                subscriptionStatusFlow = flowOf(SubscriptionStatus.Free),
            )

            // When
            viewModel.onEvent(ContactSupportUiEvent.OnSendEmailClick)

            // Then
            assertEquals(
                listOf(
                    "contact_support_email_opened" to mapOf<String, Any>(
                        "is_logged_in" to false,
                        "is_pro" to false,
                    ),
                ),
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN a logged in pro user WHEN sending the support email THEN logs is_logged_in and is_pro as true`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                sessionStatus = authenticated(),
                subscriptionStatusFlow = flowOf(
                    SubscriptionStatus.Pro(
                        planName = null,
                        purchaseDate = null,
                        expirationDate = null,
                    ),
                ),
            )

            // When
            viewModel.onEvent(ContactSupportUiEvent.OnSendEmailClick)

            // Then
            assertEquals(
                listOf(
                    "contact_support_email_opened" to mapOf<String, Any>(
                        "is_logged_in" to true,
                        "is_pro" to true,
                    ),
                ),
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN the contact sheet WHEN copying support email THEN logs contact_support_email_copied`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                sessionStatus = SessionStatus.Initializing,
                subscriptionStatusFlow = emptyFlow(),
            )

            // When
            viewModel.onEvent(ContactSupportUiEvent.OnCopyEmailClick)

            // Then
            assertEquals(
                listOf("contact_support_email_copied" to emptyMap<String, Any>()),
                trackedEvents,
            )
        }

    private fun authenticated() = SessionStatus.Authenticated(
        session = UserSession(
            accessToken = "",
            refreshToken = "",
            expiresIn = 0,
            tokenType = "",
            user = UserInfo(
                aud = "",
                id = "user-1",
                email = "user@test.com",
                userMetadata = buildJsonObject { put("name", "Test User") },
            ),
        ),
    )

    private fun prepareScenario(
        sessionStatus: SessionStatus,
        subscriptionStatusFlow: Flow<SubscriptionStatus?>,
    ) {
        val collected = mutableListOf<Pair<String, Map<String, Any>>>()
        trackedEvents = collected
        viewModel = ContactSupportViewModel(
            uiStateFactory = ContactSupportUiStateFactory(
                getSubscriptionStatusFlow = { subscriptionStatusFlow },
                getAppLanguageFlow = { flowOf(Language.ENGLISH) },
                sessionStatus = MutableStateFlow(sessionStatus),
                sessionUserMapper = SessionUserMapper(),
                platform = Platform.Android,
            ),
            mailtoFactory = FakeContactSupportMailtoFactory(),
            trackEvent = TrackEvent { name, params -> collected += name to params },
        )
    }
}

private class FakeContactSupportMailtoFactory : ContactSupportMailtoFactory {
    override suspend fun create(state: ContactSupportUiState): String = "mailto:support@test.com"
}
