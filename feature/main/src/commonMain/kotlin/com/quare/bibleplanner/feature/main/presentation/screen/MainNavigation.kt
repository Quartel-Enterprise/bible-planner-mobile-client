package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.profile.domain.model.photoBytes
import com.quare.bibleplanner.core.profile.domain.model.photoUrl
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationIcon
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent
import com.quare.bibleplanner.ui.component.ProfileAvatar
import org.jetbrains.compose.resources.stringResource

private val profileIconSize = 24.dp

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
                MainNavigationItemIcon(
                    icon = presentationItem.icon,
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

@Composable
private fun MainNavigationItemIcon(
    icon: MainNavigationIcon,
    contentDescription: String,
) {
    when (icon) {
        is MainNavigationIcon.Vector -> {
            Icon(
                imageVector = icon.imageVector,
                contentDescription = contentDescription,
            )
        }

        is MainNavigationIcon.Profile -> {
            ProfileAvatar(
                photoUrl = icon.avatar.photoUrl,
                photoBytes = icon.avatar.photoBytes,
                displayName = icon.displayName,
                size = profileIconSize,
                fallbackIcon = Icons.Default.Person,
            )
        }
    }
}
