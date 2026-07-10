package com.quare.bibleplanner.feature.contactsupport.presentation.factory.impl

import bibleplanner.feature.contact_support.generated.resources.Res
import bibleplanner.feature.contact_support.generated.resources.diagnostics_account_label
import bibleplanner.feature.contact_support.generated.resources.diagnostics_account_not_connected
import bibleplanner.feature.contact_support.generated.resources.diagnostics_app_version_label
import bibleplanner.feature.contact_support.generated.resources.diagnostics_language_label
import bibleplanner.feature.contact_support.generated.resources.diagnostics_platform_label
import bibleplanner.feature.contact_support.generated.resources.diagnostics_subscription_label
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.feature.contactsupport.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.BuildSupportEmailBody
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiState
import com.quare.bibleplanner.feature.contactsupport.presentation.model.toStringResource
import org.jetbrains.compose.resources.getString

internal class BuildSupportEmailBodyImpl : BuildSupportEmailBody {
    override suspend fun invoke(state: ContactSupportUiState): String {
        val appVersionLabel = getString(Res.string.diagnostics_app_version_label)
        val platformLabel = getString(Res.string.diagnostics_platform_label)
        val platformValue = getString(state.platform.toStringResource())
        val languageLabel = getString(Res.string.diagnostics_language_label)
        val languageValue = state.selectedLanguage
            .valueOrNull()
            ?.let { getString(it.toStringResource()) }
            .orEmpty()
        val accountLabel = getString(Res.string.diagnostics_account_label)
        val accountValue = when (val accountStatus = state.accountStatusModel) {
            is AccountStatusModel.LoggedIn -> accountStatus.user.email

            AccountStatusModel.LoggedOut, AccountStatusModel.Loading, AccountStatusModel.Error ->
                getString(Res.string.diagnostics_account_not_connected)
        }
        val subscriptionLabel = getString(Res.string.diagnostics_subscription_label)
        val subscriptionValue = state.subscriptionStatus
            .valueOrNull()
            ?.let {
                getString(
                    it.toStringResource(),
                )
            }.orEmpty()

        return buildString {
            appendLine()
            appendLine()
            appendLine("---")
            appendLine("$appVersionLabel: ${state.appVersion}")
            appendLine("$platformLabel: $platformValue")
            appendLine("$languageLabel: $languageValue")
            appendLine("$accountLabel: $accountValue")
            append("$subscriptionLabel: $subscriptionValue")
        }
    }
}
