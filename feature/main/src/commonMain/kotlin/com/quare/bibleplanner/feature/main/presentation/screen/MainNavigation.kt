package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MainNavigationBar(
    modifier: Modifier,
    selectedRoute: NavKey?,
    mainNavigationModels: List<MainNavigationItemModel<NavKey>>,
    language: Language,
    onEvent: (MainScreenUiEvent) -> Unit,
) {
    NavigationBar(modifier = modifier) {
        key(language) {
            MainNavigationItemsComponent(
                mainNavigationModels = mainNavigationModels,
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
internal fun MainNavigationRail(
    selectedRoute: NavKey?,
    mainNavigationModels: List<MainNavigationItemModel<NavKey>>,
    language: Language,
    onEvent: (MainScreenUiEvent) -> Unit,
) {
    NavigationRail {
        key(language) {
            MainNavigationItemsComponent(
                mainNavigationModels = mainNavigationModels,
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

@Composable
private fun MainNavigationItemsComponent(
    mainNavigationModels: List<MainNavigationItemModel<NavKey>>,
    isItemSelected: (MainNavigationItemModel<NavKey>) -> Boolean,
    onEvent: (MainScreenUiEvent) -> Unit,
    itemFactory: @Composable (Boolean, () -> Unit, @Composable () -> Unit, @Composable () -> Unit) -> Unit,
) {
    mainNavigationModels.forEach { bottomNavigationItemModel ->
        val presentationItem = bottomNavigationItemModel.presentationModel
        val isSelected = isItemSelected(bottomNavigationItemModel)
        itemFactory(
            isSelected,
            {
                onEvent(
                    MainScreenUiEvent.BottomNavItemClicked(
                        bottomNavigationItemModel.route,
                    ),
                )
            },
            {
                Icon(
                    imageVector = presentationItem.icon,
                    contentDescription = stringResource(presentationItem.title),
                )
            },
            {
                Text(
                    stringResource(presentationItem.title),
                    textAlign = TextAlign.Center,
                )
            },
        )
    }
}
