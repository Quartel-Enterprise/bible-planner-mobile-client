package com.quare.bibleplanner.feature.more.presentation.factory

import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.premium_and_support
import bibleplanner.feature.more.generated.resources.premium_section
import bibleplanner.feature.more.generated.resources.support_section
import bibleplanner.feature.more.generated.resources.theme_dark
import bibleplanner.feature.more.generated.resources.theme_light
import bibleplanner.feature.more.generated.resources.theme_system
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlanStartDateFlowUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsInstagramLinkVisibleUseCase
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.feature.more.domain.usecase.ShouldShowDonateOptionUseCase
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Clock

internal class MoreUiStateFactory(
    private val isFreeUser: IsFreeUserUseCase,
    private val isInstagramLinkVisible: IsInstagramLinkVisibleUseCase,
    private val shouldShowDonateOption: ShouldShowDonateOptionUseCase,
    private val getPlanStartDate: GetPlanStartDateFlowUseCase,
    private val getThemeOptionFlow: GetThemeOptionFlow,
) {
    fun create(): Flow<MoreUiState> = combine(
        flow { emit(isFreeUser()) },
        flow { emit(isInstagramLinkVisible()) },
        flow { emit(shouldShowDonateOption()) },
        getPlanStartDate(),
        getThemeOptionFlow(),
    ) { isFree, isInstagramVisible, shouldShowDonate, startDate, theme ->
        val headerRes = when {
            isFree && shouldShowDonate -> Res.string.premium_and_support
            isFree -> Res.string.premium_section
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
            isFreeUser = isFree,
            isInstagramLinkVisible = isInstagramVisible,
            shouldShowDonateOption = shouldShowDonate,
            headerRes = headerRes,
        )
    }

    private fun Theme.toStringResource(): StringResource = when (this) {
        Theme.LIGHT -> Res.string.theme_light
        Theme.DARK -> Res.string.theme_dark
        Theme.SYSTEM -> Res.string.theme_system
    }
}
