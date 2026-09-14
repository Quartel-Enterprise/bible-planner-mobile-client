package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemModel

@Composable
internal actual fun NativeTabBarAvatarEffect(
    mainNavigationModels: List<MainNavigationItemModel<NavKey>>,
    tabBarItems: List<UIKitUITabBarItem>,
) = Unit
