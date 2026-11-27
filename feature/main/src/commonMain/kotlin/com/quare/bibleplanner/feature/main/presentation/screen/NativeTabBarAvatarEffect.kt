package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemModel

@Composable
internal expect fun NativeTabBarAvatarEffect(
    mainNavigationModels: List<MainNavigationItemModel<MainNavRouteDestination>>,
    tabBarItems: List<UIKitUITabBarItem>,
)
