package com.quare.bibleplanner.feature.more.presentation

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
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.become_pro_part_1
import bibleplanner.feature.more.generated.resources.become_pro_part_2
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.feature.more.presentation.utils.MoreUiActionCollector
import com.quare.bibleplanner.feature.more.presentation.viewmodel.MoreViewModel
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import com.quare.bibleplanner.ui.utils.MainTabScaffold
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
fun EntryProviderScope<NavKey>.more(
    onNavigate: (NavKey) -> Unit,
    navigationBar: @Composable (Modifier) -> Unit,
    navigationRail: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    entry<BottomNavRoute.More> {
        MoreTabContent(
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
private fun MoreTabContent(
    onNavigate: (NavKey) -> Unit,
    navigationBar: @Composable (Modifier) -> Unit,
    navigationRail: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val viewModel = koinViewModel<MoreViewModel>()
    MoreUiActionCollector(
        uiActionFlow = viewModel.uiAction,
        onNavigate = onNavigate,
        snackbarHostState = LocalSnackbarHostState.current,
    )
    val uiState by viewModel.uiState.collectAsState()

    MainTabScaffold(
        navigationBar = navigationBar,
        navigationRail = navigationRail,
    ) {
        MoreScreen(
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
