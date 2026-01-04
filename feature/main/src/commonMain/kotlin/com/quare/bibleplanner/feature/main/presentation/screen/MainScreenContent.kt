package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import com.quare.bibleplanner.ui.utils.MainScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.feature.main.presentation.model.BottomNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent
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
    val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = mainScaffoldState.topBar.value,
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
                    }
                    .onGloballyPositioned { coordinates ->
                        scrollBehavior.state.heightOffsetLimit = -coordinates.size.height.toFloat()
                    },
            ) {
                bottomNavigationModels.forEach { bottomNavigationItemModel: BottomNavigationItemModel<Any> ->
                    val presentationItem = bottomNavigationItemModel.presentationModel
                    val isSelected = isSelected(currentDestination, bottomNavigationItemModel)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            onEvent(
                                MainScreenUiEvent.BottomNavItemClicked(
                                    bottomNavigationItemModel.route,
                                ),
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = presentationItem.icon,
                                contentDescription = stringResource(presentationItem.title),
                            )
                        },
                        label = {
                            Text(
                                stringResource(presentationItem.title),
                                textAlign = TextAlign.Center,
                            )
                        },
                    )
                }
            }
        },
        content = content,
    )
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
