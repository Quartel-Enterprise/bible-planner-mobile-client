package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.AdaptiveNavigationBar
import com.mohamedrejeb.calf.ui.navigation.UIKitTabBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.profile.domain.model.photoBytes
import com.quare.bibleplanner.core.profile.domain.model.photoUrl
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationIcon
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent
import com.quare.bibleplanner.ui.component.ProfileAvatarImage
import org.jetbrains.compose.resources.stringResource

private val profileIconSize = 24.dp

@OptIn(ExperimentalCalfUiApi::class)
@Composable
internal fun MainNavigationBar(
    modifier: Modifier,
    isNativeBarVisible: Boolean,
    selectedRoute: NavKey?,
    mainNavigationModels: List<MainNavigationItemModel<MainNavRouteDestination>>,
    language: Language,
    onEvent: (MainScreenUiEvent) -> Unit,
) {
    val density = LocalDensity.current
    var barHeight by rememberSaveable { mutableFloatStateOf(0f) }
    if (!isNativeBarVisible) {
        Spacer(
            modifier = modifier
                .fillMaxWidth()
                .height(with(density) { barHeight.toDp() }),
        )
        return
    }
    Box(
        modifier = modifier.onSizeChanged { size ->
            if (size.height > 0) barHeight = size.height.toFloat()
        },
    ) {
        key(language) {
            val iosItems = mainNavigationModels.map { it.toUIKitUITabBarItem() }
            AdaptiveNavigationBar(
                iosItems = iosItems,
                iosSelectedIndex = mainNavigationModels.indexOfFirst { it.route == selectedRoute },
                iosOnItemSelected = { index ->
                    onEvent(MainScreenUiEvent.BottomNavItemClicked(mainNavigationModels[index].route))
                },
                iosConfiguration = UIKitTabBarConfiguration(
                    selectedItemColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                MainNavigationItemsComponent(
                    mainNavigationModels = mainNavigationModels,
                    isItemSelected = { it.route == selectedRoute },
                    onEvent = onEvent,
                ) { modifier, selected, onClick, icon, label ->
                    NavigationBarItem(
                        modifier = modifier,
                        selected = selected,
                        onClick = onClick,
                        icon = icon,
                        label = label,
                    )
                }
            }
            NativeTabBarAvatarEffect(
                mainNavigationModels = mainNavigationModels,
                tabBarItems = iosItems,
            )
        }
    }
}

@Composable
private fun MainNavigationItemModel<MainNavRouteDestination>.toUIKitUITabBarItem(): UIKitUITabBarItem =
    UIKitUITabBarItem(
        title = stringResource(route.toTabTitle()),
        image = UIKitImage.SystemName(presentationModel.iosIcon.symbolName),
        selectedImage = UIKitImage.SystemName(presentationModel.iosIcon.selectedSymbolName),
    )

@Composable
internal fun MainNavigationRail(
    modifier: Modifier,
    selectedRoute: NavKey?,
    mainNavigationModels: List<MainNavigationItemModel<MainNavRouteDestination>>,
    language: Language,
    onEvent: (MainScreenUiEvent) -> Unit,
) {
    NavigationRail(modifier = modifier) {
        key(language) {
            MainNavigationItemsComponent(
                mainNavigationModels = mainNavigationModels,
                isItemSelected = { it.route == selectedRoute },
                onEvent = onEvent,
            ) { modifier, selected, onClick, icon, label ->
                NavigationRailItem(
                    modifier = modifier,
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
    mainNavigationModels: List<MainNavigationItemModel<MainNavRouteDestination>>,
    isItemSelected: (MainNavigationItemModel<MainNavRouteDestination>) -> Boolean,
    onEvent: (MainScreenUiEvent) -> Unit,
    itemFactory: @Composable (Modifier, Boolean, () -> Unit, @Composable () -> Unit, @Composable () -> Unit) -> Unit,
) {
    mainNavigationModels.forEach { bottomNavigationItemModel ->
        val presentationItem = bottomNavigationItemModel.presentationModel
        val isSelected = isItemSelected(bottomNavigationItemModel)
        itemFactory(
            Modifier.testTag(presentationItem.testTag),
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
                    route = bottomNavigationItemModel.route,
                    contentDescription = stringResource(bottomNavigationItemModel.route.toTabTitle()),
                )
            },
            {
                Text(
                    stringResource(bottomNavigationItemModel.route.toTabTitle()),
                    textAlign = TextAlign.Center,
                )
            },
        )
    }
}

@Composable
private fun MainNavigationItemIcon(
    icon: MainNavigationIcon,
    route: MainNavRouteDestination,
    contentDescription: String,
) {
    when (icon) {
        MainNavigationIcon.Default -> {
            Icon(
                imageVector = route.toTabIcon(),
                contentDescription = contentDescription,
            )
        }

        is MainNavigationIcon.Profile -> {
            ProfileAvatarImage(
                photoUrl = icon.avatar.photoUrl,
                photoBytes = icon.avatar.photoBytes,
                displayName = icon.displayName,
                size = profileIconSize,
                fallbackIcon = route.toTabIcon(),
            )
        }
    }
}
