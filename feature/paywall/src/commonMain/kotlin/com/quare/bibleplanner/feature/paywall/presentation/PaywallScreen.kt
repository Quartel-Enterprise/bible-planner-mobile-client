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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.Dp
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
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallLandscapeDimensions
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

private val landscapeMinWidth = 600.dp
private val compactLandscapeMaxHeight = 480.dp
private val valuePanelMaxWidth = 400.dp
private val portraitFeatureSpacing = 20.dp
private val portraitFeatureIconSize = 30.dp
private val portraitPlansSpacing = 12.dp
private val portraitActionButtonHeight = 56.dp
private const val VALUE_PANEL_WIDTH_FRACTION = 0.44f

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
                dimensions = if (maxHeight < compactLandscapeMaxHeight) {
                    PaywallLandscapeDimensions.Compact
                } else {
                    PaywallLandscapeDimensions.Regular
                },
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
                    buttonHeight = portraitActionButtonHeight,
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
            PremiumFeaturesList(
                maxFreeNotes = (uiState as? PaywallUiState.Success)?.maxFreeNotes,
                itemSpacing = portraitFeatureSpacing,
                iconSize = portraitFeatureIconSize,
            )
            VerticalSpacer(28)
            if (uiState is PaywallUiState.Success) {
                SubscriptionPlans(
                    subscriptionPlans = uiState.subscriptionPlans,
                    onEvent = onEvent,
                    itemSpacing = portraitPlansSpacing,
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
    dimensions: PaywallLandscapeDimensions,
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
                    .fillMaxWidth(VALUE_PANEL_WIDTH_FRACTION)
                    .widthIn(max = valuePanelMaxWidth)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = dimensions.panelPaddingHorizontal,
                        vertical = dimensions.panelPaddingVertical,
                    ),
            ) {
                PaywallHero(
                    modifier = Modifier.fillMaxWidth(),
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    titleFontSize = dimensions.heroTitleFontSize,
                    titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    proColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    subtitleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    iconBoxSize = dimensions.heroIconBoxSize,
                    iconBoxCornerRadius = dimensions.heroIconBoxCornerRadius,
                    iconBoxColor = MaterialTheme.colorScheme.surface,
                    iconSize = dimensions.heroIconSize,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    horizontalAlignment = Alignment.Start,
                    textAlign = TextAlign.Start,
                )
                VerticalSpacer(dimensions.heroBottomSpacing)
                PremiumFeaturesList(
                    maxFreeNotes = (uiState as? PaywallUiState.Success)?.maxFreeNotes,
                    titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    subtitleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    itemSpacing = dimensions.featureSpacing,
                    iconSize = dimensions.featureIconSize,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = dimensions.contentPaddingHorizontal,
                            end = dimensions.contentPaddingHorizontal,
                            top = dimensions.contentPaddingTop,
                            bottom = 12.dp,
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.choose_your_plan),
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = dimensions.headerTitleFontSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    PaywallCloseButton(
                        buttonSize = dimensions.closeButtonSize,
                        iconSize = dimensions.closeIconSize,
                        onClick = { onEvent(PaywallUiEvent.OnBackClick) },
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = dimensions.contentPaddingHorizontal,
                            end = dimensions.contentPaddingHorizontal,
                            top = 14.dp,
                            bottom = 6.dp,
                        ),
                ) {
                    if (uiState is PaywallUiState.Success) {
                        SubscriptionPlans(
                            subscriptionPlans = uiState.subscriptionPlans,
                            onEvent = onEvent,
                            itemSpacing = dimensions.plansSpacing,
                        )
                    }
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                PaywallActionSectionComponent(
                    uiState = uiState,
                    onEvent = onEvent,
                    buttonHeight = dimensions.actionButtonHeight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(
                            start = dimensions.contentPaddingHorizontal,
                            end = dimensions.contentPaddingHorizontal,
                            top = 12.dp,
                            bottom = dimensions.contentPaddingBottom,
                        ),
                )
            }
        }
    }
}

@Composable
private fun PaywallCloseButton(
    buttonSize: Dp,
    iconSize: Dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            imageVector = Icons.Rounded.Close,
            contentDescription = stringResource(Res.string.close),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
