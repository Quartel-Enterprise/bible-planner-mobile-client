package com.quare.bibleplanner.feature.paywall.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.choose_your_plan
import bibleplanner.feature.paywall.generated.resources.close
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.paywall.presentation.component.PaywallActionSectionComponent
import com.quare.bibleplanner.feature.paywall.presentation.component.PaywallHero
import com.quare.bibleplanner.feature.paywall.presentation.component.premiumfeature.PremiumFeaturesList
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.SubscriptionPlans
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

private val landscapeMinWidth = 600.dp
private val valuePanelWidth = 400.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PaywallScreen(
    platform: Platform,
    snackbarHostState: SnackbarHostState,
    uiState: PaywallUiState,
    onEvent: (PaywallUiEvent) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth > landscapeMinWidth) {
            PaywallLandscapeContent(
                snackbarHostState = snackbarHostState,
                uiState = uiState,
                onEvent = onEvent,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        } else {
            PaywallPortraitContent(
                platform = platform,
                snackbarHostState = snackbarHostState,
                uiState = uiState,
                onEvent = onEvent,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun PaywallPortraitContent(
    platform: Platform,
    snackbarHostState: SnackbarHostState,
    uiState: PaywallUiState,
    onEvent: (PaywallUiEvent) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackIcon(platform = platform, onBackClick = { onEvent(PaywallUiEvent.OnBackClick) })
                },
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                PaywallActionSectionComponent(
                    uiState = uiState,
                    onEvent = onEvent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 14.dp,
                            bottom = 22.dp,
                        ),
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            VerticalSpacer(4)
            PaywallHero(
                modifier = Modifier.fillMaxWidth(),
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                titleFontSize = 28.sp,
                titleColor = MaterialTheme.colorScheme.onSurface,
                proColor = MaterialTheme.colorScheme.primary,
                subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                iconBoxSize = 64.dp,
                iconBoxCornerRadius = 20.dp,
                iconBoxColor = MaterialTheme.colorScheme.primaryContainer,
                iconSize = 34.dp,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            VerticalSpacer(26)
            PremiumFeaturesList(maxFreeNotes = (uiState as? PaywallUiState.Success)?.maxFreeNotes)
            VerticalSpacer(28)
            if (uiState is PaywallUiState.Success) {
                SubscriptionPlans(
                    subscriptionPlans = uiState.subscriptionPlans,
                    onEvent = onEvent,
                )
            }
            VerticalSpacer(8)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun PaywallLandscapeContent(
    snackbarHostState: SnackbarHostState,
    uiState: PaywallUiState,
    onEvent: (PaywallUiEvent) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .width(valuePanelWidth)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 44.dp, vertical = 52.dp),
            ) {
                PaywallHero(
                    modifier = Modifier.fillMaxWidth(),
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    titleFontSize = 36.sp,
                    titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    proColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    subtitleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    iconBoxSize = 60.dp,
                    iconBoxCornerRadius = 18.dp,
                    iconBoxColor = MaterialTheme.colorScheme.surface,
                    iconSize = 32.dp,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    horizontalAlignment = Alignment.Start,
                    textAlign = TextAlign.Start,
                )
                VerticalSpacer(34)
                PremiumFeaturesList(
                    maxFreeNotes = (uiState as? PaywallUiState.Success)?.maxFreeNotes,
                    titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    subtitleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(
                        start = 44.dp,
                        end = 44.dp,
                        top = 40.dp,
                        bottom = 30.dp,
                    ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.choose_your_plan),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    PaywallCloseButton(onClick = { onEvent(PaywallUiEvent.OnBackClick) })
                }
                VerticalSpacer(22)
                if (uiState is PaywallUiState.Success) {
                    SubscriptionPlans(
                        subscriptionPlans = uiState.subscriptionPlans,
                        onEvent = onEvent,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                VerticalSpacer(20)
                PaywallActionSectionComponent(
                    uiState = uiState,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PaywallCloseButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = Icons.Rounded.Close,
            contentDescription = stringResource(Res.string.close),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
