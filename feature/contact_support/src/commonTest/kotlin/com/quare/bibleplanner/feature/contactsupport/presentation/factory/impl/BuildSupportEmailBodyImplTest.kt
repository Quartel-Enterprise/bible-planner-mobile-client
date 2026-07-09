package com.quare.bibleplanner.feature.contactsupport.presentation.factory.impl

import bibleplanner.feature.contact_support.generated.resources.Res
import bibleplanner.feature.contact_support.generated.resources.diagnostics_account_label
import bibleplanner.feature.contact_support.generated.resources.diagnostics_account_not_connected
import bibleplanner.feature.contact_support.generated.resources.diagnostics_app_version_label
import bibleplanner.feature.contact_support.generated.resources.diagnostics_language_label
import bibleplanner.feature.contact_support.generated.resources.diagnostics_platform_label
import bibleplanner.feature.contact_support.generated.resources.diagnostics_subscription_free
import bibleplanner.feature.contact_support.generated.resources.diagnostics_subscription_label
import bibleplanner.feature.contact_support.generated.resources.diagnostics_subscription_pro
import bibleplanner.feature.contact_support.generated.resources.platform_android
import bibleplanner.feature.contact_support.generated.resources.platform_ios
import bibleplanner.feature.preferences.app_language.generated.resources.language_english
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.user.domain.model.UserModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.contactsupport.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiState
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import bibleplanner.feature.preferences.app_language.generated.resources.Res as AppLanguageRes

internal class BuildSupportEmailBodyImplTest {
    private lateinit var buildSupportEmailBody: BuildSupportEmailBodyImpl

    @Test
    fun `GIVEN a logged-in pro user WHEN building the body THEN interpolates every diagnostics line`() = runTest {
        // Given
        prepareScenario()
        val state = ContactSupportUiState(
            accountStatusModel = AccountStatusModel.LoggedIn(
                user = UserModel(id = "user-1", name = "Ana", email = "ana@example.com", photo = null),
            ),
            subscriptionStatus = Loadable.Loaded(
                SubscriptionStatus.Pro(planName = "Pro", purchaseDate = null, expirationDate = null, willRenew = true),
            ),
            selectedLanguage = Loadable.Loaded(Language.ENGLISH),
            appVersion = "1.19.1",
            platform = Platform.Android,
        )

        // When
        val body = buildSupportEmailBody(state)

        // Then
        val expected = buildString {
            appendLine()
            appendLine()
            appendLine("---")
            appendLine("${getString(Res.string.diagnostics_app_version_label)}: 1.19.1")
            appendLine(
                "${getString(Res.string.diagnostics_platform_label)}: ${getString(Res.string.platform_android)}",
            )
            appendLine(
                "${getString(Res.string.diagnostics_language_label)}: " +
                    getString(AppLanguageRes.string.language_english),
            )
            appendLine("${getString(Res.string.diagnostics_account_label)}: ana@example.com")
            append(
                "${getString(Res.string.diagnostics_subscription_label)}: " +
                    getString(Res.string.diagnostics_subscription_pro),
            )
        }
        assertEquals(expected, body)
    }

    @Test
    fun `GIVEN a logged-out free user with a loading language WHEN building the body THEN falls back gracefully`() =
        runTest {
            // Given
            prepareScenario()
            val state = ContactSupportUiState(
                accountStatusModel = AccountStatusModel.LoggedOut,
                subscriptionStatus = Loadable.Loaded(SubscriptionStatus.Free),
                selectedLanguage = Loadable.Loading,
                appVersion = "1.0.0",
                platform = Platform.Ios,
            )

            // When
            val body = buildSupportEmailBody(state)

            // Then
            val expected = buildString {
                appendLine()
                appendLine()
                appendLine("---")
                appendLine("${getString(Res.string.diagnostics_app_version_label)}: 1.0.0")
                appendLine(
                    "${getString(Res.string.diagnostics_platform_label)}: ${getString(Res.string.platform_ios)}",
                )
                appendLine("${getString(Res.string.diagnostics_language_label)}: ")
                appendLine(
                    "${getString(Res.string.diagnostics_account_label)}: " +
                        getString(Res.string.diagnostics_account_not_connected),
                )
                append(
                    "${getString(Res.string.diagnostics_subscription_label)}: " +
                        getString(Res.string.diagnostics_subscription_free),
                )
            }
            assertEquals(expected, body)
        }

    private fun prepareScenario() {
        buildSupportEmailBody = BuildSupportEmailBodyImpl()
    }
}
