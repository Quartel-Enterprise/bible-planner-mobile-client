package com.quare.bibleplanner.feature.main.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.main.generated.resources.Res
import bibleplanner.feature.main.generated.resources.more
import bibleplanner.feature.main.generated.resources.plans
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.feature.main.presentation.model.BottomNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.model.BottomNavigationItemPresentationModel
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiAction
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class MainScreenViewModel : ViewModel() {
    private val routes: List<BottomNavRoute> = listOf(
        BottomNavRoute.Plans,
        BottomNavRoute.More,
    )

    private val _uiAction: MutableSharedFlow<MainScreenUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<MainScreenUiAction> = _uiAction

    val bottomNavigationItemModels: List<BottomNavigationItemModel<Any>> =
        routes.map { it.toBottomNavHost() }

    fun dispatchUiEvent(event: MainScreenUiEvent) {
        when (event) {
            is MainScreenUiEvent.BottomNavItemClicked -> emitAction(
                MainScreenUiAction.NavigateToBottomRoute(
                    event.route,
                ),
            )
        }
    }

    private fun emitAction(action: MainScreenUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }

    private fun BottomNavRoute.toBottomNavHost(): BottomNavigationItemModel<Any> = BottomNavigationItemModel(
        route = this,
        presentationModel = when (this) {
            BottomNavRoute.Plans -> BottomNavigationItemPresentationModel(
                title = Res.string.plans,
                icon = Icons.Default.DateRange,
            )

            BottomNavRoute.More -> BottomNavigationItemPresentationModel(
                title = Res.string.more,
                icon = Icons.Default.Menu,
            )
        },
    )
}
