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
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlanStartDateFlowUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetSubscriptionStatusFlowUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsInstagramLinkVisibleUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsProVerificationRequiredUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.IsMoreWebAppEnabled
import com.quare.bibleplanner.feature.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow
import com.quare.bibleplanner.feature.materialyou.domain.usecase.IsDynamicColorSupported
import com.quare.bibleplanner.feature.more.domain.usecase.ShouldShowDonateOptionUseCase
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetContrastTypeFlow
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Clock

internal class MoreUiStateFactory(
    private val getSubscriptionStatusFlow: GetSubscriptionStatusFlowUseCase?,
    private val isInstagramLinkVisible: IsInstagramLinkVisibleUseCase,
    private val shouldShowDonateOption: ShouldShowDonateOptionUseCase,
    private val getPlanStartDate: GetPlanStartDateFlowUseCase,
    private val getThemeOptionFlow: GetThemeOptionFlow,
    private val getContrastTypeFlow: GetContrastTypeFlow,
    private val getIsDynamicColorsEnabledFlow: GetIsDynamicColorsEnabledFlow,
    private val isProVerificationRequired: IsProVerificationRequiredUseCase,
    private val isMoreWebAppEnabled: IsMoreWebAppEnabled,
    private val isDynamicColorSupported: IsDynamicColorSupported,
) {
    fun create(): Flow<MoreUiState> {
        val configFlow = flow {
            coroutineScope {
                val instagram = async { isInstagramLinkVisible() }
                val donate = async { shouldShowDonateOption() }
                val isPro = async { isProVerificationRequired() }
                val isWebAppEnabled = async { isMoreWebAppEnabled() }
                emit(
                    Config(
                        isInstagramVisible = instagram.await(),
                        shouldShowDonate = donate.await(),
                        isProVerificationRequired = isPro.await(),
                        isWebAppVisible = isWebAppEnabled.await(),
                    ),
                )
            }
        }

        val themeFlow = combine(
            getThemeOptionFlow(),
            getContrastTypeFlow(),
            getIsDynamicColorsEnabledFlow(),
        ) { theme, contrast, isDynamic -> Triple(theme, contrast, isDynamic) }

        return combine(
            getSubscriptionStatusFlow?.invoke() ?: flowOf(null),
            getPlanStartDate(),
            themeFlow,
            configFlow,
        ) { subscriptionStatus, startDate, (theme, contrast, isDynamicColorsEnabled), config ->
            val headerRes = when {
                config.isProVerificationRequired && config.shouldShowDonate -> Res.string.pro_and_support
                config.isProVerificationRequired -> Res.string.pro_section
                config.shouldShowDonate -> Res.string.support_section
                else -> null
            }

            MoreUiState.Loaded(
                themeRes = theme.toStringResource(),
                contrastRes = when {
                    isDynamicColorsEnabled && isDynamicColorSupported() -> Res.string.dynamic_colors
                    else -> contrast.toStringResource()
                },
                planStartDate = startDate,
                currentDate = Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date,
                subscriptionStatus = subscriptionStatus,
                isInstagramLinkVisible = config.isInstagramVisible,
                shouldShowDonateOption = config.shouldShowDonate,
                headerRes = headerRes,
                isProCardVisible = config.isProVerificationRequired,
                isWebAppVisible = config.isWebAppVisible,
            )
        }
    }

    private data class Config(
        val isInstagramVisible: Boolean,
        val shouldShowDonate: Boolean,
        val isProVerificationRequired: Boolean,
        val isWebAppVisible: Boolean,
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
