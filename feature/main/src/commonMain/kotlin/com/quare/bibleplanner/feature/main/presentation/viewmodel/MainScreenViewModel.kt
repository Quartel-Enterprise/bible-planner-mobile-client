package com.quare.bibleplanner.feature.main.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.main.generated.resources.Res
import bibleplanner.feature.main.generated.resources.books
import bibleplanner.feature.main.generated.resources.more
import bibleplanner.feature.main.generated.resources.plans
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemPresentationModel
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiAction
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainScreenViewModel(
    getAppLanguageFlow: GetAppLanguageFlow,
) : ViewModel() {
    val languageState: StateFlow<Language> = getAppLanguageFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Language.ENGLISH,
    )

    private val _uiAction: MutableSharedFlow<MainScreenUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<MainScreenUiAction> = _uiAction

    val mainNavigationItemModels: List<MainNavigationItemModel<NavKey>> =
        listOf(
            MainNavRouteDestination.Plans,
            MainNavRouteDestination.Books,
            MainNavRouteDestination.More,
        ).map(::mapToMainNavigationItemModel)

    fun dispatchUiEvent(event: MainScreenUiEvent) {
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

    private fun mapToMainNavigationItemModel(bottomNavRoute: MainNavRouteDestination): MainNavigationItemModel<NavKey> =
        bottomNavRoute.run {
            MainNavigationItemModel(
                route = this,
                presentationModel = when (this) {
                    MainNavRouteDestination.Plans -> MainNavigationItemPresentationModel(
                        title = Res.string.plans,
                        icon = Icons.Default.DateRange,
                    )

                    MainNavRouteDestination.Books -> MainNavigationItemPresentationModel(
                        title = Res.string.books,
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                    )

                    MainNavRouteDestination.More -> MainNavigationItemPresentationModel(
                        title = Res.string.more,
                        icon = Icons.Default.Menu,
                    )
                },
            )
        }
}
