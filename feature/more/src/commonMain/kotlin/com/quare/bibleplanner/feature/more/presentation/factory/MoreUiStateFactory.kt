package com.quare.bibleplanner.feature.more.presentation.factory

import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.contrast_high
import bibleplanner.feature.more.generated.resources.contrast_medium
import bibleplanner.feature.more.generated.resources.contrast_standard
import bibleplanner.feature.more.generated.resources.dynamic_colors
import bibleplanner.feature.more.generated.resources.pro_and_support
import bibleplanner.feature.more.generated.resources.pro_section
import bibleplanner.feature.more.generated.resources.support_section
import bibleplanner.feature.more.generated.resources.theme_dark
import bibleplanner.feature.more.generated.resources.theme_light
import bibleplanner.feature.more.generated.resources.theme_system
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleFlowUseCase
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlanStartDateFlowUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetSubscriptionStatusFlowUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveInstagramLinkVisible
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveProVerificationRequired
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.ObserveMoreWebAppEnabled
import com.quare.bibleplanner.core.user.data.mapper.SessionUserMapper
import com.quare.bibleplanner.feature.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow
import com.quare.bibleplanner.feature.materialyou.domain.usecase.IsDynamicColorSupported
import com.quare.bibleplanner.feature.more.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.more.domain.usecase.GetSelectedVersionDownloadedChaptersFlowUseCase
import com.quare.bibleplanner.feature.more.domain.usecase.ObserveShowDonateOptionUseCase
import com.quare.bibleplanner.feature.more.generated.MoreBuildKonfig
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetContrastTypeFlow
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Clock

internal class MoreUiStateFactory(
    private val getSubscriptionStatusFlow: GetSubscriptionStatusFlowUseCase?,
    private val isInstagramLinkVisible: ObserveInstagramLinkVisible,
    private val shouldShowDonateOption: ObserveShowDonateOptionUseCase,
    private val getPlanStartDate: GetPlanStartDateFlowUseCase,
    private val getThemeOptionFlow: GetThemeOptionFlow,
    private val getContrastTypeFlow: GetContrastTypeFlow,
    private val getIsDynamicColorsEnabledFlow: GetIsDynamicColorsEnabledFlow,
    private val isProVerificationRequired: ObserveProVerificationRequired,
    private val isMoreWebAppEnabled: ObserveMoreWebAppEnabled,
    private val isDynamicColorSupported: IsDynamicColorSupported,
    private val sessionStatus: StateFlow<SessionStatus>,
    private val sessionUserMapper: SessionUserMapper,
    private val bibleVersionDao: BibleVersionDao,
    private val getSelectedVersionDownloadedChapters: GetSelectedVersionDownloadedChaptersFlowUseCase,
    private val getSelectedBible: GetSelectedBibleFlowUseCase,
    private val getAppLanguageFlow: GetAppLanguageFlow,
) {
    fun create(): Flow<MoreUiState> {
        val moreScreenFlows: Flow<MoreScreenConfiguration> = combine(
            getMoreScreenRemoteConfigsFlow(),
            getThemeConfigurationFlow(),
            sessionStatus,
            getSelectedBible(),
            bibleVersionDao.getAllVersionsFlow(),
        ) { remoteConfigs, themeConfiguration, sessionStatus, selectedBible, allVersions ->
            MoreScreenConfiguration(
                remoteConfigs = remoteConfigs,
                themeConfiguration = themeConfiguration,
                sessionStatus = sessionStatus,
                selectedBible = selectedBible,
                allVersions = allVersions,
            )
        }

        return combine(
            getSubscriptionStatusFlow?.invoke() ?: flowOf(null),
            getPlanStartDate(),
            moreScreenFlows,
            getSelectedVersionDownloadedChapters(),
            getAppLanguageFlow(),
        ) { subscriptionStatus, startDate, moreScreenConfiguration, downloadedChaptersCount, selectedLanguage ->
            val remoteConfigs = moreScreenConfiguration.remoteConfigs
            val themeConfiguration = moreScreenConfiguration.themeConfiguration
            val shouldShowDonate = remoteConfigs.shouldShowDonate
            val isProVerificationRequired = remoteConfigs.isProVerificationRequired
            val headerRes = when {
                isProVerificationRequired && shouldShowDonate -> Res.string.pro_and_support
                isProVerificationRequired -> Res.string.pro_section
                shouldShowDonate -> Res.string.support_section
                else -> null
            }

            val selectedBible = moreScreenConfiguration.selectedBible
            val bibleVersionEntity = moreScreenConfiguration.allVersions.find { it.id == selectedBible?.version?.id }
            val downloadProgress = when (bibleVersionEntity?.status) {
                DownloadStatus.DONE, DownloadStatus.NOT_STARTED, null -> null
                else -> downloadedChaptersCount.toFloat() / bibleVersionEntity.totalChapters
            }

            MoreUiState.Loaded(
                themeRes = themeConfiguration.theme.toStringResource(),
                contrastRes = when {
                    themeConfiguration.isDynamicColorsEnabled && isDynamicColorSupported() -> Res.string.dynamic_colors
                    else -> themeConfiguration.contrast.toStringResource()
                },
                planStartDate = startDate,
                currentDate = Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date,
                subscriptionStatus = subscriptionStatus,
                isInstagramLinkVisible = remoteConfigs.isInstagramVisible,
                shouldShowDonateOption = remoteConfigs.shouldShowDonate,
                headerRes = headerRes,
                isProCardVisible = remoteConfigs.isProVerificationRequired,
                isWebAppVisible = remoteConfigs.isWebAppVisible,
                appVersion = MoreBuildKonfig.APP_VERSION,
                accountStatusModel = when (val sessionStatus = moreScreenConfiguration.sessionStatus) {
                    is SessionStatus.Authenticated -> {
                        sessionStatus.session.user
                            ?.let(sessionUserMapper::map)
                            ?.let(AccountStatusModel::LoggedIn) ?: AccountStatusModel.Error
                    }

                    SessionStatus.Initializing -> {
                        AccountStatusModel.Loading
                    }

                    is SessionStatus.NotAuthenticated -> {
                        AccountStatusModel.LoggedOut
                    }

                    is SessionStatus.RefreshFailure -> {
                        AccountStatusModel.Error
                    }
                },
                bibleVersionName = selectedBible?.version?.name,
                bibleDownloadProgress = downloadProgress,
                selectedLanguage = selectedLanguage,
            )
        }
    }

    private fun getThemeConfigurationFlow(): Flow<ThemeConfiguration> = combine(
        getThemeOptionFlow(),
        getContrastTypeFlow(),
        getIsDynamicColorsEnabledFlow(),
    ) { theme, contrast, isDynamic -> ThemeConfiguration(theme, contrast, isDynamic) }

    private fun getMoreScreenRemoteConfigsFlow(): Flow<RemoteConfigs> = combine(
        isInstagramLinkVisible(),
        shouldShowDonateOption(),
        isProVerificationRequired(),
        isMoreWebAppEnabled(),
    ) { isInstagramVisible, shouldShowDonate, isProVerificationRequired, isWebAppVisible ->
        Logger.d(tag = "MoreUiStateFactory") { "isProVerificationRequired: $isProVerificationRequired" }
        RemoteConfigs(
            isInstagramVisible = isInstagramVisible,
            shouldShowDonate = shouldShowDonate,
            isProVerificationRequired = isProVerificationRequired,
            isWebAppVisible = isWebAppVisible,
        )
    }

    private data class MoreScreenConfiguration(
        val remoteConfigs: RemoteConfigs,
        val themeConfiguration: ThemeConfiguration,
        val sessionStatus: SessionStatus,
        val selectedBible: BibleModel?,
        val allVersions: List<BibleVersionEntity>,
    )

    private data class RemoteConfigs(
        val isInstagramVisible: Boolean,
        val shouldShowDonate: Boolean,
        val isProVerificationRequired: Boolean,
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
