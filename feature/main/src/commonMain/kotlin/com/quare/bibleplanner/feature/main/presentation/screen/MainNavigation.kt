package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.AdaptiveNavigationBar
import com.mohamedrejeb.calf.ui.navigation.UIKitTabBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import com.quare.bibleplanner.core.profile.domain.model.photoBytes
import com.quare.bibleplanner.core.profile.domain.model.photoUrl
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationIcon
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent
import com.quare.bibleplanner.ui.component.ProfileAvatar
import org.jetbrains.compose.resources.stringResource

private val profileIconSize = 24.dp

@OptIn(ExperimentalCalfUiApi::class)
@Composable
internal fun MainNavigationBar(
    modifier: Modifier,
    isNativeBarVisible: Boolean,
    selectedRoute: NavKey?,
    mainNavigationModels: List<MainNavigationItemModel<NavKey>>,
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
                ) { selected, onClick, icon, label ->
                    NavigationBarItem(
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
private fun MainNavigationItemModel<NavKey>.toUIKitUITabBarItem(): UIKitUITabBarItem = UIKitUITabBarItem(
    title = stringResource(presentationModel.title),
    image = UIKitImage.SystemName(presentationModel.iosIcon.symbolName),
    selectedImage = UIKitImage.SystemName(presentationModel.iosIcon.selectedSymbolName),
)

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
