package com.quare.bibleplanner.feature.main.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.calf.sf.symbols.SFSymbol
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.presentation.bridge.FlowWatcher
import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.profile.domain.usecase.ObserveUserProfile
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationIcon
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationIosIcon
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

class MainScreenViewModel internal constructor(
    getAppLanguageFlow: GetAppLanguageFlow,
    observeUserProfile: ObserveUserProfile,
    trackEvent: TrackEvent,
) : TrackedViewModel<MainScreenUiEvent>(trackEvent) {
    val languageState: StateFlow<Language> = getAppLanguageFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Language.ENGLISH,
    )

    val uiAction: SharedFlow<MainScreenUiAction>
        field = MutableSharedFlow<MainScreenUiAction>()

    val mainNavigationItemModels: StateFlow<List<MainNavigationItemModel<MainNavRouteDestination>>> =
        observeUserProfile().map(::toMainNavigationItemModels).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = toMainNavigationItemModels(null),
        )

    // Why: SwiftUI can't collect a Flow; it observes the tabs and actions through these, built only when it asks.
    val stateWatcher: FlowWatcher<List<MainNavigationItemModel<MainNavRouteDestination>>> by lazy {
        FlowWatcher(mainNavigationItemModels, viewModelScope)
    }

    val actionWatcher: FlowWatcher<MainScreenUiAction> by lazy { FlowWatcher(uiAction, viewModelScope) }

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
            uiAction.emit(action)
        }
    }

    private fun toMainNavigationItemModels(
        userProfile: UserProfile?,
    ): List<MainNavigationItemModel<MainNavRouteDestination>> = listOf(
        MainNavRouteDestination.Plans,
        MainNavRouteDestination.Books,
        MainNavRouteDestination.Profile,
    ).map { route -> route.toMainNavigationItemModel(userProfile) }

    private fun MainNavRouteDestination.toMainNavigationItemModel(
        userProfile: UserProfile?,
    ): MainNavigationItemModel<MainNavRouteDestination> = MainNavigationItemModel(
        route = this,
        presentationModel = when (this) {
            MainNavRouteDestination.Plans -> MainNavigationItemPresentationModel(
                icon = MainNavigationIcon.Default,
                iosIcon = MainNavigationIosIcon(
                    symbolName = SFSymbol.calendar,
                    selectedSymbolName = SFSymbol.calendar,
                ),
                testTag = PLANS_TAB_TAG,
            )

            MainNavRouteDestination.Books -> MainNavigationItemPresentationModel(
                icon = MainNavigationIcon.Default,
                iosIcon = MainNavigationIosIcon(
                    symbolName = SFSymbol.book,
                    selectedSymbolName = SFSymbol.bookFill,
                ),
                testTag = BOOKS_TAB_TAG,
            )

            MainNavRouteDestination.Profile -> MainNavigationItemPresentationModel(
                icon = MainNavigationIcon.Profile(
                    avatar = userProfile?.avatar ?: AvatarSource.None,
                    displayName = userProfile?.displayName,
                ),
                iosIcon = MainNavigationIosIcon(
                    symbolName = SFSymbol.personCropCircle,
                    selectedSymbolName = SFSymbol.personCropCircleFill,
                ),
                testTag = PROFILE_TAB_TAG,
            )
        },
    )

    private companion object {
        const val PLANS_TAB_TAG = "plans_tab"
        const val BOOKS_TAB_TAG = "books_tab"
        const val PROFILE_TAB_TAG = "profile_tab"
    }
}
