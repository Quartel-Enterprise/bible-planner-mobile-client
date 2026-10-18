package com.quare.bibleplanner.feature.contactsupport.presentation.factory

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetSubscriptionStatusFlowUseCase
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.user.data.mapper.SessionUserMapper
import com.quare.bibleplanner.feature.contactsupport.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.contactsupport.generated.ContactSupportBuildKonfig
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiState
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import software.quare.loadable.Loadable
import software.quare.loadable.toLoaded

internal class ContactSupportUiStateFactory(
    private val getSubscriptionStatusFlow: GetSubscriptionStatusFlowUseCase?,
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val sessionStatus: StateFlow<SessionStatus>,
    private val sessionUserMapper: SessionUserMapper,
    private val platform: Platform,
) {
    fun createInitialState(): ContactSupportUiState = ContactSupportUiState(
        accountStatusModel = AccountStatusModel.Loading,
        subscriptionStatus = Loadable.Loading,
        selectedLanguage = Loadable.Loading,
        appVersion = ContactSupportBuildKonfig.APP_VERSION,
        platform = platform,
    )

    fun create(): Flow<ContactSupportUiState> = merge(
        observeSubscriptionStatus().map { status ->
            { state: ContactSupportUiState -> state.copy(subscriptionStatus = status.toLoaded()) }
        },
        getAppLanguageFlow().map { language ->
            { state: ContactSupportUiState -> state.copy(selectedLanguage = language.toLoaded()) }
        },
        sessionStatus.map { status ->
            { state: ContactSupportUiState -> state.copy(accountStatusModel = status.toAccountStatusModel()) }
        },
    ).scan(createInitialState()) { state, reduce -> reduce(state) }

    private fun observeSubscriptionStatus(): Flow<SubscriptionStatus?> =
        getSubscriptionStatusFlow?.invoke() ?: flowOf(null)

    private fun SessionStatus.toAccountStatusModel(): AccountStatusModel = when (this) {
        is SessionStatus.Authenticated -> {
            session.user
                ?.let(sessionUserMapper::map)
                ?.let(AccountStatusModel::LoggedIn) ?: AccountStatusModel.Error
        }

        SessionStatus.Initializing -> AccountStatusModel.Loading

        is SessionStatus.NotAuthenticated -> AccountStatusModel.LoggedOut

        is SessionStatus.RefreshFailure -> AccountStatusModel.Error
    }
}
