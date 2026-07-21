package com.quare.bibleplanner.feature.main.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.main.generated.resources.Res
import bibleplanner.feature.main.generated.resources.books
import bibleplanner.feature.main.generated.resources.plans
import bibleplanner.feature.main.generated.resources.profile
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.profile.domain.usecase.ObserveUserProfile
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationIcon
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemPresentationModel
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiAction
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainScreenViewModel(
    getAppLanguageFlow: GetAppLanguageFlow,
    observeUserProfile: ObserveUserProfile,
    trackEvent: TrackEvent,
) : TrackedViewModel<MainScreenUiEvent>(trackEvent) {
    val languageState: StateFlow<Language> = getAppLanguageFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Language.ENGLISH,
    )

    private val _uiAction: MutableSharedFlow<MainScreenUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<MainScreenUiAction> = _uiAction

    val mainNavigationItemModels: StateFlow<List<MainNavigationItemModel<NavKey>>> =
        observeUserProfile().map(::toMainNavigationItemModels).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = toMainNavigationItemModels(null),
        )

    override fun handleEvent(event: MainScreenUiEvent) {
        when (event) {
            is MainScreenUiEvent.BottomNavItemClicked -> {
                emitAction(
                    MainScreenUiAction.NavigateToBottomRoute(
                        event.route,
                    ),
                )
            }
        }
    }

    private fun emitAction(action: MainScreenUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }

    private fun toMainNavigationItemModels(userProfile: UserProfile?): List<MainNavigationItemModel<NavKey>> = listOf(
        MainNavRouteDestination.Plans,
        MainNavRouteDestination.Books,
        MainNavRouteDestination.Profile,
    ).map { route -> route.toMainNavigationItemModel(userProfile) }

    private fun MainNavRouteDestination.toMainNavigationItemModel(
        userProfile: UserProfile?,
    ): MainNavigationItemModel<NavKey> = MainNavigationItemModel(
        route = this,
        presentationModel = when (this) {
            MainNavRouteDestination.Plans -> MainNavigationItemPresentationModel(
                title = Res.string.plans,
                icon = MainNavigationIcon.Vector(Icons.Default.DateRange),
            )

            MainNavRouteDestination.Books -> MainNavigationItemPresentationModel(
                title = Res.string.books,
                icon = MainNavigationIcon.Vector(Icons.AutoMirrored.Filled.MenuBook),
            )

            MainNavRouteDestination.Profile -> MainNavigationItemPresentationModel(
                title = Res.string.profile,
                icon = MainNavigationIcon.Profile(
                    avatar = userProfile?.avatar ?: AvatarSource.None,
                    displayName = userProfile?.displayName,
                ),
            )
        },
    )
}
