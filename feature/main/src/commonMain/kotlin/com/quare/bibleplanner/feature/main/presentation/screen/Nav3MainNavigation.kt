package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.main.presentation.model.BottomNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent

@Composable
internal fun Nav3MainNavigationBar(
    modifier: Modifier,
    selectedRoute: Any?,
    bottomNavigationModels: List<BottomNavigationItemModel<Any>>,
    language: Language,
    onEvent: (MainScreenUiEvent) -> Unit,
) {
    NavigationBar(modifier = modifier) {
        key(language) {
            MainNavigationItems(
                bottomNavigationModels = bottomNavigationModels,
                isItemSelected = { it.route == selectedRoute },
                onEvent = onEvent,
            ) { selected, onClick, icon, label ->
                NavigationBarItem(
                    selected = selected,
                    onClick = onClick,
                    icon = icon,
                    label = label,
                )
            }
        }
    }
}

@Composable
internal fun Nav3MainNavigationRail(
    selectedRoute: Any?,
    bottomNavigationModels: List<BottomNavigationItemModel<Any>>,
    language: Language,
    onEvent: (MainScreenUiEvent) -> Unit,
) {
    NavigationRail {
        key(language) {
            MainNavigationItems(
                bottomNavigationModels = bottomNavigationModels,
                isItemSelected = { it.route == selectedRoute },
                onEvent = onEvent,
            ) { selected, onClick, icon, label ->
                NavigationRailItem(
                    selected = selected,
                    onClick = onClick,
                    icon = icon,
                    label = label,
                )
            }
        }
    }
}
