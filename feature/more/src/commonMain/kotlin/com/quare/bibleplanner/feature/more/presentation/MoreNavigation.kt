package com.quare.bibleplanner.feature.more.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.become_pro_part_1
import bibleplanner.feature.more.generated.resources.become_pro_part_2
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.feature.more.presentation.utils.MoreUiActionCollector
import com.quare.bibleplanner.feature.more.presentation.viewmodel.MoreViewModel
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.utils.MainScaffoldState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.more(
    navController: NavController,
    mainScaffoldState: MainScaffoldState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    composable<BottomNavRoute.More> {
        val viewModel = koinViewModel<MoreViewModel>()
        MoreUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            navController = navController,
            snackbarHostState = mainScaffoldState.snackbarHostState,
        )
        val uiState by viewModel.uiState.collectAsState()

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
