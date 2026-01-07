package com.quare.bibleplanner.feature.more.presentation.factory

import bibleplanner.feature.more.generated.resources.Res
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
import com.quare.bibleplanner.feature.more.domain.usecase.ShouldShowDonateOptionUseCase
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeOptionFlow
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
    private val getSubscriptionStatusFlow: GetSubscriptionStatusFlowUseCase,
    private val isInstagramLinkVisible: IsInstagramLinkVisibleUseCase,
    private val shouldShowDonateOption: ShouldShowDonateOptionUseCase,
    private val getPlanStartDate: GetPlanStartDateFlowUseCase,
    private val getThemeOptionFlow: GetThemeOptionFlow,
    private val isProVerificationRequiredUseCase: IsProVerificationRequiredUseCase,
) {
    fun create(): Flow<MoreUiState> {
        val configFlow = flow {
            coroutineScope {
                val instagram = async { isInstagramLinkVisible() }
                val donate = async { shouldShowDonateOption() }
                val isPro = async { isProVerificationRequiredUseCase() }
                emit(
                    Triple(
                        instagram.await(),
                        donate.await(),
                        isPro.await(),
                    ),
                )
            }
        }

        return combine(
            getSubscriptionStatusFlow(),
            getPlanStartDate(),
            getThemeOptionFlow(),
            configFlow,
        ) { subscriptionStatus, startDate, theme, config ->
            val (isInstagramVisible, shouldShowDonate, isProVerificationRequired) = config

            val headerRes = when {
                isProVerificationRequired && shouldShowDonate -> Res.string.pro_and_support
                isProVerificationRequired -> Res.string.pro_section
                shouldShowDonate -> Res.string.support_section
                else -> null
            }

            MoreUiState.Loaded(
                themeSubtitle = theme.toStringResource(),
                planStartDate = startDate,
                currentDate = Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date,
                subscriptionStatus = subscriptionStatus,
                isInstagramLinkVisible = isInstagramVisible,
                shouldShowDonateOption = shouldShowDonate,
                headerRes = headerRes,
                isPremiumCardVisible = isProVerificationRequired,
            )
        }
    }

    private fun Theme.toStringResource(): StringResource = when (this) {
        Theme.LIGHT -> Res.string.theme_light
        Theme.DARK -> Res.string.theme_dark
        Theme.SYSTEM -> Res.string.theme_system
    }
}
