package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    content: @Composable () -> Unit,
) {
    NavigationSuiteScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(BottomAppBarDefaults.exitAlwaysScrollBehavior().nestedScrollConnection),
        navigationSuiteItems = {
            bottomNavigationModels.forEach { bottomNavigationItemModel: BottomNavigationItemModel<Any> ->
                val presentationItem = bottomNavigationItemModel.presentationModel
                val isSelected = isSelected(currentDestination, bottomNavigationItemModel)
                item(
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
