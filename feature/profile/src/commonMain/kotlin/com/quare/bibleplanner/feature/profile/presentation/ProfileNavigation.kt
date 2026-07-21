package com.quare.bibleplanner.feature.profile.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.become_pro_part_1
import bibleplanner.feature.profile.generated.resources.become_pro_part_2
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.feature.profile.presentation.utils.ProfileUiActionCollector
import com.quare.bibleplanner.feature.profile.presentation.viewmodel.ProfileViewModel
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import com.quare.bibleplanner.ui.utils.MainTabScaffold
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
fun EntryProviderScope<NavKey>.profile(
    onNavigate: (NavKey) -> Unit,
    navigationBar: @Composable (Modifier) -> Unit,
    navigationRail: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    entry<MainNavRouteDestination.Profile> {
        ProfileTabContent(
            onNavigate = onNavigate,
            navigationBar = navigationBar,
            navigationRail = navigationRail,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ProfileTabContent(
    onNavigate: (NavKey) -> Unit,
    navigationBar: @Composable (Modifier) -> Unit,
    navigationRail: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    ProfileUiActionCollector(
        uiActionFlow = viewModel.uiAction,
        onNavigate = onNavigate,
        snackbarHostState = LocalSnackbarHostState.current,
    )
    val uiState by viewModel.uiState.collectAsState()

    MainTabScaffold(
        navigationBar = navigationBar,
        navigationRail = navigationRail,
    ) {
        ProfileScreen(
            state = uiState,
            onEvent = viewModel::onEvent,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
            becomeProTitleContent = {
                Row {
                    with(sharedTransitionScope) {
                        Text(
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "become_pro_part_1"),
                                animatedVisibilityScope = animatedContentScope,
                            ),
                            text = stringResource(Res.string.become_pro_part_1),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        HorizontalSpacer(4.dp)
                        Text(
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "become_pro_part_2"),
                                animatedVisibilityScope = animatedContentScope,
                            ),
                            text = stringResource(Res.string.become_pro_part_2),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            },
        )
    }
}
