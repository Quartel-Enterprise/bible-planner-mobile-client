package com.quare.bibleplanner.feature.more.presentation.viewmodel

import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.theme_dark
import bibleplanner.feature.more.generated.resources.theme_light
import bibleplanner.feature.more.generated.resources.theme_system
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlanStartDateUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsInstagramLinkVisibleUseCase
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiAction
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.feature.more.usecase.ShouldShowDonateOptionUseCase
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Clock

internal class MoreViewModel(
    private val isFreeUser: IsFreeUserUseCase,
    private val isInstagramLinkVisible: IsInstagramLinkVisibleUseCase,
    private val calculateBibleProgress: CalculateBibleProgressUseCase,
    private val shouldShowDonateOption: ShouldShowDonateOptionUseCase,
    private val getPlanStartDate: GetPlanStartDateUseCase,
    private val getThemeOptionFlow: GetThemeOptionFlow,
) : ViewModel() {
    private val _uiAction = MutableSharedFlow<MoreUiAction>()
    val uiAction: SharedFlow<MoreUiAction> = _uiAction
    private val _uiState = MutableStateFlow(
        MoreUiState(
            themeSubtitle = Res.string.theme_system,
            planStartDate = null,
            currentDate = Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date,
            isFreeUser = false,
            isInstagramLinkVisible = false,
            shouldShowDonateOption = false,
        ),
    )
    val uiState: StateFlow<MoreUiState> = _uiState

    init {
        viewModelScope.launch {
            val isFreeDeferred = async { isFreeUser() }
            val isInstagramVisibleDeferred = async { isInstagramLinkVisible() }
            val shouldShowDonateDeferred = async { shouldShowDonateOption() }
            _uiState.update {
                it.copy(
                    isFreeUser = isFreeDeferred.await(),
                    isInstagramLinkVisible = isInstagramVisibleDeferred.await(),
                    shouldShowDonateOption = shouldShowDonateDeferred.await(),
                )
            }
            combine(
                getPlanStartDate(),
                getThemeOptionFlow(),
            ) { startDate, theme ->
                _uiState.update {
                    it.copy(
                        planStartDate = startDate,
                        themeSubtitle = theme.toDisplayString(),
                    )
                }
            }.collect {}
        }
    }

    private fun Theme.toDisplayString(): StringResource = when (this) {
        Theme.LIGHT -> Res.string.theme_light
        Theme.DARK -> Res.string.theme_dark
        Theme.SYSTEM -> Res.string.theme_system
    }

    fun onEvent(event: MoreUiEvent) {
        when (event) {
            is MoreUiEvent.OnItemClick -> when (event.type) {
                MoreOptionItemType.THEME -> {
                    emitAction(MoreUiAction.GoToTheme)
                }

                MoreOptionItemType.PRIVACY_POLICY -> {
                    emitAction(MoreUiAction.OpenLink(PRIVACY_URL))
                }

                MoreOptionItemType.TERMS -> {
                    emitAction(MoreUiAction.OpenLink(TERMS_URL))
                }

                MoreOptionItemType.BECOME_PREMIUM -> {
                    emitAction(MoreUiAction.GoToPaywall)
                }

                MoreOptionItemType.INSTAGRAM -> {
                    emitAction(MoreUiAction.OpenLink(getInstagramUrl()))
                }

                MoreOptionItemType.EDIT_PLAN_START_DAY -> {
                    emitAction(MoreUiAction.GoToEditPlanStartDay)
                }

                MoreOptionItemType.DELETE_PROGRESS -> {
                    viewModelScope.launch {
                        val progress = calculateBibleProgress().first()
                        if (progress > 0) {
                            emitAction(MoreUiAction.GoToDeleteProgress)
                        } else {
                            emitAction(MoreUiAction.ShowNoProgressToDelete)
                        }
                    }
                }

                MoreOptionItemType.DONATE -> {
                    emitAction(MoreUiAction.OpenLink(SPONSOR_URL))
                }
            }
        }
    }

    private fun getInstagramUrl(): String = when (Locale.current.language) {
        "pt" -> INSTAGRAM_PT_BR
        "es" -> INSTAGRAM_ES
        else -> INSTAGRAM_DEFAULT
    }

    private fun emitAction(action: MoreUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }

    companion object {
        private const val BASE_URL = "https://www.bibleplanner.app"
        private const val PRIVACY_URL = "$BASE_URL/privacy"
        private const val TERMS_URL = "$BASE_URL/terms"
        private const val SPONSOR_URL = "https://github.com/sponsors/Quartel-Enterprise"
        private const val INSTAGRAM_DEFAULT = "https://www.instagram.com/bible.planner"
        private const val INSTAGRAM_PT_BR = "$INSTAGRAM_DEFAULT.brasil"
        private const val INSTAGRAM_ES = "$INSTAGRAM_DEFAULT.espanol"
    }
}
