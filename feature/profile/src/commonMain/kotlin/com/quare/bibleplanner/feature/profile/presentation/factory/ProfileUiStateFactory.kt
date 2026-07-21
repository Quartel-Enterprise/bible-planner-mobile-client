package com.quare.bibleplanner.feature.profile.presentation.factory

import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.contrast_high
import bibleplanner.feature.profile.generated.resources.contrast_medium
import bibleplanner.feature.profile.generated.resources.contrast_standard
import bibleplanner.feature.profile.generated.resources.dynamic_colors
import bibleplanner.feature.profile.generated.resources.pro_and_support
import bibleplanner.feature.profile.generated.resources.pro_section
import bibleplanner.feature.profile.generated.resources.theme_dark
import bibleplanner.feature.profile.generated.resources.theme_light
import bibleplanner.feature.profile.generated.resources.theme_system
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleFlowUseCase
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlanStartDateFlowUseCase
import com.quare.bibleplanner.core.profile.domain.usecase.ObserveUserProfile
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetSubscriptionStatusFlowUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveInstagramLinkVisible
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.ObserveProfileWebAppEnabled
import com.quare.bibleplanner.feature.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow
import com.quare.bibleplanner.feature.materialyou.domain.usecase.IsDynamicColorSupported
import com.quare.bibleplanner.feature.profile.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.profile.domain.usecase.GetSelectedVersionDownloadedChaptersFlowUseCase
import com.quare.bibleplanner.feature.profile.domain.usecase.ObserveShowDonateOptionUseCase
import com.quare.bibleplanner.feature.profile.generated.ProfileBuildKonfig
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetContrastTypeFlow
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Clock

internal class ProfileUiStateFactory(
    private val getSubscriptionStatusFlow: GetSubscriptionStatusFlowUseCase?,
    private val isInstagramLinkVisible: ObserveInstagramLinkVisible,
    private val shouldShowDonateOption: ObserveShowDonateOptionUseCase,
    private val getPlanStartDate: GetPlanStartDateFlowUseCase,
    private val getThemeOptionFlow: GetThemeOptionFlow,
    private val getContrastTypeFlow: GetContrastTypeFlow,
    private val getIsDynamicColorsEnabledFlow: GetIsDynamicColorsEnabledFlow,
    private val isProfileWebAppEnabled: ObserveProfileWebAppEnabled,
    private val isDynamicColorSupported: IsDynamicColorSupported,
    private val sessionStatus: StateFlow<SessionStatus>,
    private val observeUserProfile: ObserveUserProfile,
    private val bibleVersionDao: BibleVersionDao,
    private val getSelectedVersionDownloadedChapters: GetSelectedVersionDownloadedChaptersFlowUseCase,
    private val getSelectedBible: GetSelectedBibleFlowUseCase,
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val platform: Platform,
) {
    fun initialState(): ProfileUiState = ProfileUiState(
        accountStatusModel = AccountStatusModel.Loading,
        subscriptionStatus = Loadable.Loading,
        isProCardVisible = Loadable.Loaded(true),
        shouldShowDonateOption = Loadable.Loading,
        headerRes = Loadable.Loading,
        isInstagramLinkVisible = Loadable.Loading,
        isWebAppVisible = Loadable.Loading,
        themeRes = Loadable.Loading,
        contrastRes = Loadable.Loading,
        selectedLanguage = Loadable.Loading,
        bibleVersionName = Loadable.Loading,
        bibleDownloadProgress = Loadable.Loading,
        planStartDate = Loadable.Loading,
        currentDate = currentDate(),
        appVersion = ProfileBuildKonfig.APP_VERSION,
        isUpdateRowVisible = platform !is Platform.Desktop,
        isCheckingForUpdate = false,
    )

    fun create(): Flow<ProfileUiState> = merge(
        getProfileScreenRemoteConfigsFlow().map { remoteConfigs ->
            { state: ProfileUiState ->
                state.copy(
                    shouldShowDonateOption = Loadable.Loaded(remoteConfigs.shouldShowDonate),
                    isInstagramLinkVisible = Loadable.Loaded(remoteConfigs.isInstagramVisible),
                    isWebAppVisible = Loadable.Loaded(remoteConfigs.isWebAppVisible),
                    headerRes = Loadable.Loaded(remoteConfigs.toHeaderRes()),
                )
            }
        },
        getThemeConfigurationFlow().map { themeConfiguration ->
            { state: ProfileUiState ->
                state.copy(
                    themeRes = Loadable.Loaded(themeConfiguration.theme.toStringResource()),
                    contrastRes = Loadable.Loaded(themeConfiguration.toContrastRes()),
                )
            }
        },
        getAppLanguageFlow().map { language ->
            { state: ProfileUiState -> state.copy(selectedLanguage = Loadable.Loaded(language)) }
        },
        getPlanStartDate().map { startDate ->
            { state: ProfileUiState -> state.copy(planStartDate = Loadable.Loaded(startDate)) }
        },
        subscriptionStatusFlow().map { subscriptionStatus ->
            { state: ProfileUiState -> state.copy(subscriptionStatus = Loadable.Loaded(subscriptionStatus)) }
        },
        accountStatusFlow().map { accountStatusModel ->
            { state: ProfileUiState -> state.copy(accountStatusModel = accountStatusModel) }
        },
        getBibleRowFlow().map { bibleRow ->
            { state: ProfileUiState ->
                state.copy(
                    bibleVersionName = Loadable.Loaded(bibleRow.name),
                    bibleDownloadProgress = Loadable.Loaded(bibleRow.downloadProgress),
                )
            }
        },
    ).scan(initialState()) { state, reduce -> reduce(state) }

    private fun subscriptionStatusFlow(): Flow<SubscriptionStatus?> =
        getSubscriptionStatusFlow?.invoke() ?: flowOf(null)

    private fun getBibleRowFlow(): Flow<BibleRow> = combine(
        getSelectedBible(),
        bibleVersionDao.getAllVersionsFlow(),
        getSelectedVersionDownloadedChapters(),
    ) { selectedBible, allVersions, downloadedChaptersCount ->
        val bibleVersionEntity = allVersions.find { it.id == selectedBible?.version?.id }
        val downloadProgress = when (bibleVersionEntity?.status) {
            DownloadStatus.DONE, DownloadStatus.NOT_STARTED, null -> null
            else -> downloadedChaptersCount.toFloat() / bibleVersionEntity.totalChapters
        }
        BibleRow(
            name = selectedBible?.version?.name,
            downloadProgress = downloadProgress,
        )
    }.distinctUntilChanged()

    private fun getThemeConfigurationFlow(): Flow<ThemeConfiguration> = combine(
        getThemeOptionFlow(),
        getContrastTypeFlow(),
        getIsDynamicColorsEnabledFlow(),
    ) { theme, contrast, isDynamic -> ThemeConfiguration(theme, contrast, isDynamic) }
        .distinctUntilChanged()

    private fun getProfileScreenRemoteConfigsFlow(): Flow<RemoteConfigs> = combine(
        isInstagramLinkVisible(),
        shouldShowDonateOption(),
        isProfileWebAppEnabled(),
    ) { isInstagramVisible, shouldShowDonate, isWebAppVisible ->
        RemoteConfigs(
            isInstagramVisible = isInstagramVisible,
            shouldShowDonate = shouldShowDonate,
            isWebAppVisible = isWebAppVisible,
        )
    }.distinctUntilChanged()

    private fun currentDate(): LocalDate = Clock.System
        .now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    private fun accountStatusFlow(): Flow<AccountStatusModel> = sessionStatus.flatMapLatest { status ->
        when (status) {
            is SessionStatus.Authenticated -> observeUserProfile().map { profile ->
                profile?.let(AccountStatusModel::LoggedIn) ?: AccountStatusModel.Error
            }

            SessionStatus.Initializing -> flowOf(AccountStatusModel.Loading)

            is SessionStatus.NotAuthenticated -> flowOf(AccountStatusModel.LoggedOut)

            is SessionStatus.RefreshFailure -> flowOf(AccountStatusModel.Error)
        }
    }

    private fun RemoteConfigs.toHeaderRes(): StringResource = when {
        shouldShowDonate -> Res.string.pro_and_support
        else -> Res.string.pro_section
    }

    private fun ThemeConfiguration.toContrastRes(): StringResource? = when {
        isDynamicColorsEnabled && isDynamicColorSupported() -> Res.string.dynamic_colors
        else -> contrast.toStringResource()
    }

    private data class BibleRow(
        val name: String?,
        val downloadProgress: Float?,
    )

    private data class RemoteConfigs(
        val isInstagramVisible: Boolean,
        val shouldShowDonate: Boolean,
        val isWebAppVisible: Boolean,
    )

    private data class ThemeConfiguration(
        val theme: Theme,
        val contrast: ContrastType,
        val isDynamicColorsEnabled: Boolean,
    )

    private fun Theme.toStringResource(): StringResource = when (this) {
        Theme.LIGHT -> Res.string.theme_light
        Theme.DARK -> Res.string.theme_dark
        Theme.SYSTEM -> Res.string.theme_system
    }

    private fun ContrastType.toStringResource(): StringResource = when (this) {
        ContrastType.Standard -> Res.string.contrast_standard
        ContrastType.Medium -> Res.string.contrast_medium
        ContrastType.High -> Res.string.contrast_high
    }
}
