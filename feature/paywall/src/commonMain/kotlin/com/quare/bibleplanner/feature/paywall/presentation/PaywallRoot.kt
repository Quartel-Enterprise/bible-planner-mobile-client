package com.quare.bibleplanner.feature.paywall.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.paywall_title_part_1
import bibleplanner.feature.paywall.generated.resources.paywall_title_part_2
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.feature.paywall.presentation.utils.PaywallUiActionCollector
import com.quare.bibleplanner.feature.paywall.presentation.viewmodel.PaywallViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.paywall(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
) {
    composable<PaywallNavRoute> {
        val viewModel = koinViewModel<PaywallViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        PaywallUiActionCollector(
            actionsFlow = viewModel.uiAction,
            navController = navController,
            snackbarHostState = snackbarHostState,
        )

        PaywallScreen(
            snackbarHostState = snackbarHostState,
            uiState = uiState,
            onEvent = viewModel::onEvent,
            titleContent = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    with(sharedTransitionScope) {
                        Text(
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "become_pro_part_1"),
                                animatedVisibilityScope = this@composable,
                            ),
                            text = stringResource(Res.string.paywall_title_part_1),
                        )
                        Text(
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "become_pro_part_2"),
                                animatedVisibilityScope = this@composable,
                            ),
                            text = stringResource(Res.string.paywall_title_part_2),
                        )
                    }
                }
            },
        )
    }
}
