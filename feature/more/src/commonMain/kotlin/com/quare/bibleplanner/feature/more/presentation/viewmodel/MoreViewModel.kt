package com.quare.bibleplanner.feature.more.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.core.model.route.DonationNavRoute
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.GetWebAppUrl
import com.quare.bibleplanner.core.utils.social.SocialUtils
import com.quare.bibleplanner.feature.more.presentation.factory.MoreUiStateFactory
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiAction
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiAction.OpenLink
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MoreViewModel(
    uiStateFactory: MoreUiStateFactory,
    private val calculateBibleProgress: CalculateBibleProgressUseCase,
    private val getWebAppUrl: GetWebAppUrl,
) : ViewModel() {
    private val _uiAction = MutableSharedFlow<MoreUiAction>()
    val uiAction: SharedFlow<MoreUiAction> = _uiAction
    private val _uiState = MutableStateFlow<MoreUiState>(MoreUiState.Loading)
    val uiState: StateFlow<MoreUiState> = _uiState

    init {
        observe(uiStateFactory.create()) { state ->
            _uiState.update { state }
        }
    }

    fun onEvent(event: MoreUiEvent) {
        when (event) {
            is MoreUiEvent.OnItemClick -> {
                when (event.type) {
                    MoreOptionItemType.THEME -> {
                        goToRoute(ThemeNavRoute)
                    }

                    MoreOptionItemType.PRIVACY_POLICY -> {
                        emitAction(OpenLink(PRIVACY_URL))
                    }

                    MoreOptionItemType.TERMS -> {
                        emitAction(OpenLink(TERMS_URL))
                    }

                    MoreOptionItemType.BECOME_PRO -> {
                        goToRoute(PaywallNavRoute)
                    }

                    MoreOptionItemType.INSTAGRAM -> {
                        emitAction(OpenLink(SocialUtils.getInstagramUrl()))
                    }

                    MoreOptionItemType.EDIT_PLAN_START_DAY -> {
                        goToRoute(EditPlanStartDateNavRoute)
                    }

                    MoreOptionItemType.DELETE_PROGRESS -> {
                        deleteProgressClick()
                    }

                    MoreOptionItemType.DONATE -> {
                        goToRoute(DonationNavRoute)
                    }

                    MoreOptionItemType.WEB_APP -> {
                        viewModelScope.launch {
                            emitAction(OpenLink(getWebAppUrl()))
                        }
                    }

                    MoreOptionItemType.RELEASE_NOTES -> {
                        goToRoute(ReleaseNotesNavRoute)
                    }

                    MoreOptionItemType.BIBLE_VERSION -> {
                        goToRoute(BibleVersionSelectorRoute)
                    }
                }
            }

            MoreUiEvent.OnProCardClick -> {
                _uiState.update {
                    if (it is MoreUiState.Loaded) {
                        it.copy(showSubscriptionDetailsDialog = true)
                    } else {
                        it
                    }
                }
            }

            MoreUiEvent.OnDismissSubscriptionDetailsDialog -> {
                _uiState.update {
                    if (it is MoreUiState.Loaded) {
                        it.copy(showSubscriptionDetailsDialog = false)
                    } else {
                        it
                    }
                }
            }

            MoreUiEvent.OnLoginClick -> {
                goToRoute(LoginNavRoute)
            }
        }
    }

    private fun deleteProgressClick() {
        viewModelScope.launch {
            val progress = calculateBibleProgress().first()
            _uiAction.emit(
                if (progress > 0) {
                    MoreUiAction.GoToRoute(DeleteAllProgressNavRoute)
                } else {
                    MoreUiAction.ShowNoProgressToDelete
                },
            )
        }
    }

    private fun goToRoute(route: Any) {
        emitAction(MoreUiAction.GoToRoute(route))
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
    }
}
