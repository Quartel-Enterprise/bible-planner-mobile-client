package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.feature.main.presentation.model.BottomNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent
import com.quare.bibleplanner.ui.utils.MainScaffoldState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    currentDestination: NavDestination?,
    bottomNavigationModels: List<BottomNavigationItemModel<Any>>,
    onEvent: (MainScreenUiEvent) -> Unit,
    mainScaffoldState: MainScaffoldState,
    content: @Composable (PaddingValues) -> Unit,
) {
    BoxWithConstraints {
        if (maxWidth > 600.dp) {
            WideMainScreenContent(
                currentDestination = currentDestination,
                bottomNavigationModels = bottomNavigationModels,
                onEvent = onEvent,
                mainScaffoldState = mainScaffoldState,
                content = content,
            )
        } else {
            NarrowMainScreenContent(
                currentDestination = currentDestination,
                bottomNavigationModels = bottomNavigationModels,
                onEvent = onEvent,
                mainScaffoldState = mainScaffoldState,
                content = content,
            )
        }
    }
}

@Composable
private fun WideMainScreenContent(
    currentDestination: NavDestination?,
    bottomNavigationModels: List<BottomNavigationItemModel<Any>>,
    onEvent: (MainScreenUiEvent) -> Unit,
    mainScaffoldState: MainScaffoldState,
    content: @Composable (PaddingValues) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail {
            MainNavigationItems(
                bottomNavigationModels = bottomNavigationModels,
                currentDestination = currentDestination,
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
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = mainScaffoldState.fab.value,
            snackbarHost = { SnackbarHost(mainScaffoldState.snackbarHostState) },
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NarrowMainScreenContent(
    currentDestination: NavDestination?,
    bottomNavigationModels: List<BottomNavigationItemModel<Any>>,
    onEvent: (MainScreenUiEvent) -> Unit,
    mainScaffoldState: MainScaffoldState,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = -scrollBehavior.state.heightOffset
                    },
            ) {
                mainScaffoldState.fab.value()
            }
        },
        snackbarHost = { SnackbarHost(mainScaffoldState.snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = -scrollBehavior.state.heightOffset
                    }.onGloballyPositioned { coordinates ->
                        scrollBehavior.state.heightOffsetLimit = -coordinates.size.height.toFloat()
                    },
            ) {
                MainNavigationItems(
                    bottomNavigationModels = bottomNavigationModels,
                    currentDestination = currentDestination,
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
        },
        content = content,
    )
}

@Composable
private fun MainNavigationItems(
    bottomNavigationModels: List<BottomNavigationItemModel<Any>>,
    currentDestination: NavDestination?,
    onEvent: (MainScreenUiEvent) -> Unit,
    itemFactory: @Composable (Boolean, () -> Unit, @Composable () -> Unit, @Composable () -> Unit) -> Unit,
) {
    bottomNavigationModels.forEach { bottomNavigationItemModel ->
        val presentationItem = bottomNavigationItemModel.presentationModel
        val isSelected = isSelected(currentDestination, bottomNavigationItemModel)
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

private fun isSelected(
    currentDestination: NavDestination?,
    bottomNavigationItemModel: BottomNavigationItemModel<Any>,
): Boolean = currentDestination
    ?.hierarchy
    ?.any { navDestination: NavDestination ->
        (bottomNavigationItemModel.route as? BottomNavRoute)?.let { route ->
            navDestination.hasRoute(route::class)
        } ?: false
    } ?: false
