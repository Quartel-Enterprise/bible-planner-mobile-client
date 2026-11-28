package com.quare.bibleplanner.android.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.avatar.ProfileAvatarImage
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.profile.domain.model.photoBytes
import com.quare.bibleplanner.core.profile.domain.model.photoUrl
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationIcon
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent
import com.quare.bibleplanner.ui.utils.LocalHostedTabBarHeight
import com.quare.bibleplanner.ui.utils.LocalHostedTabBarHeightOffset
import com.quare.bibleplanner.ui.utils.LocalRevealHostedTabBar

// Why: the same breakpoint as the shared MainTabScaffold, so the rail shows up where it does today.
private val wideScreenWidth = 600.dp
private val tabIconSize = 24.dp
private val noBarHeightOffset: () -> Float = { 0f }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainContent(
    tabItems: List<MainNavigationItemModel<MainNavRouteDestination>>,
    selectedTab: MainNavRouteDestination,
    onEvent: (MainScreenUiEvent) -> Unit,
    tabs: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    var barHeight by remember { mutableStateOf(0.dp) }
    // Why: the same behavior as the shared NarrowTabScaffold, so scrolling a tab down slides the bar away.
    val barScrollController = rememberMainBarScrollController()
    val barState = barScrollController.state
    val barHeightOffset = remember(barState) { { barState.heightOffset } }
    val revealBar = remember(barScrollController) { barScrollController::reveal }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > wideScreenWidth
        // Why: a tab the bar was hidden on, or a rail that replaced it, would otherwise leave a new tab without it.
        LaunchedEffect(
            selectedTab,
            isWide,
        ) {
            barScrollController.show()
        }
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(if (isWide) Modifier else Modifier.nestedScroll(barScrollController.nestedScrollConnection)),
            ) {
                // Why: the tabs draw under the bar, which pads for the gesture bar itself, so they scroll under both.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(
                            if (isWide) {
                                NavigationRailDefaults.windowInsets.only(WindowInsetsSides.Start)
                            } else {
                                NavigationBarDefaults.windowInsets.only(WindowInsetsSides.Bottom)
                            },
                        ),
                ) {
                    CompositionLocalProvider(
                        LocalHostedTabBarHeight provides if (isWide) 0.dp else barHeight,
                        LocalHostedTabBarHeightOffset provides if (isWide) noBarHeightOffset else barHeightOffset,
                        LocalRevealHostedTabBar provides revealBar,
                        content = tabs,
                    )
                }
                if (!isWide) {
                    ExpressiveTheme {
                        MainNavigationBar(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .graphicsLayer { translationY = -barState.heightOffset }
                                .onSizeChanged { size ->
                                    barHeight = with(density) { size.height.toDp() }
                                    barState.heightOffsetLimit = -size.height.toFloat()
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

        is MainNavigationIcon.Profile -> ProfileAvatarImage(
            photoUrl = icon.avatar.photoUrl,
            photoBytes = icon.avatar.photoBytes,
            displayName = icon.displayName,
            size = tabIconSize,
            modifier = Modifier.semantics { this.contentDescription = contentDescription },
            fallbackIcon = tabItem.route.toIcon(),
            // Why: without a photo or a name the tab shows a plain icon, tinted like the other tabs' icons.
            fallbackIconTint = LocalContentColor.current,
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
