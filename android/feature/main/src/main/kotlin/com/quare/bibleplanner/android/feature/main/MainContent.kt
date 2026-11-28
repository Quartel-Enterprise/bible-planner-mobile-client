package com.quare.bibleplanner.android.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationIcon
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent
import com.quare.bibleplanner.ui.utils.LocalHostedTabBarHeight

// Why: the same breakpoint as the shared MainTabScaffold, so the rail shows up where it does today.
private val wideScreenWidth = 600.dp
private val tabIconSize = 24.dp

@Composable
internal fun MainContent(
    tabItems: List<MainNavigationItemModel<MainNavRouteDestination>>,
    selectedTab: MainNavRouteDestination,
    onEvent: (MainScreenUiEvent) -> Unit,
    tabs: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    var barHeight by remember { mutableStateOf(0.dp) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > wideScreenWidth
        /*
         * Why: the tabs stay in the same slot whether the rail or the bar shows, so rotating the device keeps
         * the inner NavDisplay and every tab's saved state.
         */
        Row(modifier = Modifier.fillMaxSize()) {
            if (isWide) {
                ExpressiveTheme {
                    MainNavigationRail(
                        tabItems = tabItems,
                        selectedTab = selectedTab,
                        onEvent = onEvent,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .consumeWindowInsets(
                            if (isWide) {
                                NavigationRailDefaults.windowInsets.only(WindowInsetsSides.Start)
                            } else {
                                NavigationBarDefaults.windowInsets.only(WindowInsetsSides.Bottom)
                            },
                        ),
                ) {
                    CompositionLocalProvider(
                        value = LocalHostedTabBarHeight provides if (isWide) 0.dp else barHeight,
                        content = tabs,
                    )
                }
                if (!isWide) {
                    ExpressiveTheme {
                        MainNavigationBar(
                            modifier = Modifier.onSizeChanged { size ->
                                barHeight = with(density) { size.height.toDp() }
                            },
                            tabItems = tabItems,
                            selectedTab = selectedTab,
                            onEvent = onEvent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainNavigationBar(
    modifier: Modifier,
    tabItems: List<MainNavigationItemModel<MainNavRouteDestination>>,
    selectedTab: MainNavRouteDestination,
    onEvent: (MainScreenUiEvent) -> Unit,
) {
    NavigationBar(modifier = modifier) {
        tabItems.forEach { tabItem ->
            NavigationBarItem(
                modifier = Modifier.testTag(tabItem.presentationModel.testTag),
                selected = tabItem.route == selectedTab,
                onClick = { onEvent(MainScreenUiEvent.BottomNavItemClicked(tabItem.route)) },
                icon = { MainTabIcon(tabItem) },
                label = { MainTabLabel(tabItem.route) },
            )
        }
    }
}

@Composable
private fun MainNavigationRail(
    tabItems: List<MainNavigationItemModel<MainNavRouteDestination>>,
    selectedTab: MainNavRouteDestination,
    onEvent: (MainScreenUiEvent) -> Unit,
) {
    NavigationRail {
        tabItems.forEach { tabItem ->
            NavigationRailItem(
                modifier = Modifier.testTag(tabItem.presentationModel.testTag),
                selected = tabItem.route == selectedTab,
                onClick = { onEvent(MainScreenUiEvent.BottomNavItemClicked(tabItem.route)) },
                icon = { MainTabIcon(tabItem) },
                label = { MainTabLabel(tabItem.route) },
            )
        }
    }
}

@Composable
private fun MainTabLabel(route: MainNavRouteDestination) {
    Text(
        text = stringResource(route.toTitleRes()),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun MainTabIcon(tabItem: MainNavigationItemModel<MainNavRouteDestination>) {
    val contentDescription = stringResource(tabItem.route.toTitleRes())
    when (val icon = tabItem.presentationModel.icon) {
        MainNavigationIcon.Default -> Icon(
            imageVector = tabItem.route.toIcon(),
            contentDescription = contentDescription,
        )

        is MainNavigationIcon.Profile -> MainTabAvatarImage(
            avatar = icon.avatar,
            displayName = icon.displayName,
            size = tabIconSize,
            fallbackIcon = tabItem.route.toIcon(),
            contentDescription = contentDescription,
        )
    }
}

private fun MainNavRouteDestination.toTitleRes(): Int = when (this) {
    MainNavRouteDestination.Plans -> R.string.main_plans
    MainNavRouteDestination.Books -> R.string.main_books
    MainNavRouteDestination.Profile -> R.string.main_profile
}

private fun MainNavRouteDestination.toIcon(): ImageVector = when (this) {
    MainNavRouteDestination.Plans -> Icons.Default.DateRange
    MainNavRouteDestination.Books -> Icons.AutoMirrored.Filled.MenuBook
    MainNavRouteDestination.Profile -> Icons.Default.Person
}
