package com.quare.bibleplanner.feature.paywall.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.choose_your_plan
import bibleplanner.feature.paywall.generated.resources.what_you_unlock
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.paywall.presentation.component.PaywallActionSectionComponent
import com.quare.bibleplanner.feature.paywall.presentation.component.PaywallErrorCard
import com.quare.bibleplanner.feature.paywall.presentation.component.PaywallLandscapeValuePanel
import com.quare.bibleplanner.feature.paywall.presentation.component.PaywallLegalFooter
import com.quare.bibleplanner.feature.paywall.presentation.component.PaywallTopBar
import com.quare.bibleplanner.feature.paywall.presentation.component.premiumfeature.PremiumFeaturesList
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.StartProButton
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.SubscriptionPlans
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.SubscriptionPlansRow
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallLandscapeDimensions
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import com.quare.bibleplanner.feature.paywall.presentation.utils.selectedPlanPriceDescription
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.utils.ReserveBottomOverlayHeight
import org.jetbrains.compose.resources.stringResource

private val landscapeMinWidth = 600.dp
private val compactLandscapeMaxHeight = 480.dp
private val valuePanelMaxWidth = 400.dp
private val portraitFeatureSpacing = 18.dp
private val portraitFeatureIconSize = 28.dp
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
                platform = platform,
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PaywallPortraitContent(
    platform: Platform,
    snackbarHostState: SnackbarHostState,
    uiState: PaywallUiState,
    onEvent: (PaywallUiEvent) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    var actionBarHeightPx by remember { mutableFloatStateOf(0f) }
    ReserveBottomOverlayHeight { actionBarHeightPx }
    Scaffold(
        topBar = {
            PaywallTopBar(
                platform = platform,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onBackClick = { onEvent(PaywallUiEvent.OnBackClick) },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { size -> actionBarHeightPx = size.height.toFloat() }
                    .background(MaterialTheme.colorScheme.surface),
            ) {
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
            VerticalSpacer(18)
            if (uiState is PaywallUiState.Success) {
                SubscriptionPlans(
                    subscriptionPlans = uiState.subscriptionPlans,
                    onEvent = onEvent,
                    itemSpacing = portraitPlansSpacing,
                )
                VerticalSpacer(26)
            }
            Text(
                text = stringResource(Res.string.what_you_unlock),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalSpacer(14)
            PremiumFeaturesList(
                maxFreeNotes = (uiState as? PaywallUiState.Success)?.maxFreeNotes,
                itemSpacing = portraitFeatureSpacing,
                iconSize = portraitFeatureIconSize,
            )
            VerticalSpacer(8)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PaywallLandscapeContent(
    platform: Platform,
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
            PaywallLandscapeValuePanel(
                modifier = Modifier
                    .fillMaxWidth(VALUE_PANEL_WIDTH_FRACTION)
                    .widthIn(max = valuePanelMaxWidth)
                    .fillMaxHeight(),
                platform = platform,
                maxFreeNotes = (uiState as? PaywallUiState.Success)?.maxFreeNotes,
                dimensions = dimensions,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onBackClick = { onEvent(PaywallUiEvent.OnBackClick) },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                if (dimensions.centersPlanBlock) {
                    Spacer(modifier = Modifier.weight(1f))
                }
                Text(
                    modifier = Modifier.padding(
                        start = dimensions.contentPaddingHorizontal,
                        end = dimensions.contentPaddingHorizontal,
                        top = dimensions.contentPaddingTop,
                        bottom = dimensions.headerBottomPadding,
                    ),
                    text = stringResource(Res.string.choose_your_plan),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = dimensions.headerTitleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                PaywallLandscapePlanSection(
                    uiState = uiState,
                    onEvent = onEvent,
                    dimensions = dimensions,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (uiState is PaywallUiState.Success) {
                    PaywallLegalFooter(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = dimensions.contentPaddingHorizontal,
                                end = dimensions.contentPaddingHorizontal,
                                bottom = dimensions.contentPaddingBottom,
                            ),
                        storeName = uiState.storeName,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaywallLandscapePlanSection(
    uiState: PaywallUiState,
    onEvent: (PaywallUiEvent) -> Unit,
    dimensions: PaywallLandscapeDimensions,
) {
    val contentModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = dimensions.contentPaddingHorizontal)
    when (uiState) {
        is PaywallUiState.Success -> {
            SubscriptionPlansRow(
                modifier = contentModifier,
                subscriptionPlans = uiState.subscriptionPlans,
                onEvent = onEvent,
                itemSpacing = dimensions.plansSpacing,
                priceFontSize = dimensions.planPriceFontSize,
                priceUnitFontSize = dimensions.planPriceUnitFontSize,
            )
            StartProButton(
                modifier = contentModifier
                    .padding(top = dimensions.actionButtonTopPadding)
                    .height(dimensions.actionButtonHeight),
                isLoading = uiState.isPurchasing,
                priceDescription = uiState.selectedPlanPriceDescription(),
                onClick = { onEvent(PaywallUiEvent.OnStartProJourneyClick) },
            )
        }

        PaywallUiState.Loading -> Box(
            modifier = contentModifier,
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        PaywallUiState.Error -> PaywallErrorCard(modifier = contentModifier)
    }
}
