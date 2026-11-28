package com.quare.bibleplanner.android.feature.paywall

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.dialog.rememberAlertDialogResolution
import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import com.quare.bibleplanner.ui.utils.ReserveBottomOverlayHeightEffect

private val landscapeMinWidth = 600.dp
private val compactLandscapeMaxHeight = 480.dp
private val valuePanelMaxWidth = 400.dp
private val portraitFeatureSpacing = 18.dp
private val portraitFeatureIconSize = 28.dp
private val portraitPlansSpacing = 12.dp
private val portraitActionButtonHeight = 56.dp
private const val VALUE_PANEL_WIDTH_FRACTION = 0.44f

@Composable
internal fun PaywallContent(
    uiState: PaywallUiState,
    snackbarHostState: SnackbarHostState,
    failedAttempts: Int,
    onEvent: (PaywallUiEvent) -> Unit,
) {
    val isPurchasing = (uiState as? PaywallUiState.Success)?.isPurchasing == true
    // Why: the ViewModel pops the screen asynchronously, so a double tap on Back would also pop the screen below.
    val backGuard = rememberAlertDialogResolution()
    val purchaseTapGuard = rememberPurchaseTapGuard(
        isPurchasing = isPurchasing,
        failedAttempts = failedAttempts,
    )
    val actions = PaywallActions(
        onBackClick = { backGuard.resolve { onEvent(PaywallUiEvent.OnBackClick) } },
        onPlanSelected = { planType -> onEvent(PaywallUiEvent.OnPlanSelected(planType)) },
        onStartProClick = {
            purchaseTapGuard.run(isPurchasing) { onEvent(PaywallUiEvent.OnStartProJourneyClick) }
        },
        onRestoreClick = {
            purchaseTapGuard.run(isPurchasing) { onEvent(PaywallUiEvent.OnRestorePurchases) }
        },
    )
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth > landscapeMinWidth) {
            PaywallLandscapeContent(
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                actions = actions,
                dimensions = if (maxHeight < compactLandscapeMaxHeight) {
                    PaywallLandscapeDimensions.Compact
                } else {
                    PaywallLandscapeDimensions.Regular
                },
            )
        } else {
            PaywallPortraitContent(
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                actions = actions,
            )
        }
    }
}

private class PaywallActions(
    val onBackClick: () -> Unit,
    val onPlanSelected: (SubscriptionPlanType) -> Unit,
    val onStartProClick: () -> Unit,
    val onRestoreClick: () -> Unit,
)

@Composable
private fun PaywallPortraitContent(
    uiState: PaywallUiState,
    snackbarHostState: SnackbarHostState,
    actions: PaywallActions,
) {
    var actionBarHeightPx by remember { mutableFloatStateOf(0f) }
    ReserveBottomOverlayHeightEffect { actionBarHeightPx }
    Scaffold(
        topBar = { PaywallTopBar(onBackClick = actions.onBackClick) },
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
                PaywallActionSection(
                    uiState = uiState,
                    buttonHeight = portraitActionButtonHeight,
                    onStartProClick = actions.onStartProClick,
                    onRestoreClick = actions.onRestoreClick,
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
            Spacer(modifier = Modifier.height(18.dp))
            if (uiState is PaywallUiState.Success) {
                SubscriptionPlanList(
                    subscriptionPlans = uiState.subscriptionPlans,
                    onPlanSelected = actions.onPlanSelected,
                    itemSpacing = portraitPlansSpacing,
                )
                Spacer(modifier = Modifier.height(26.dp))
                Text(
                    text = stringResource(R.string.paywall_what_you_unlock),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(14.dp))
                PremiumFeatureList(
                    premiumFeatures = uiState.premiumFeatures,
                    itemSpacing = portraitFeatureSpacing,
                    iconSize = portraitFeatureIconSize,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PaywallLandscapeContent(
    uiState: PaywallUiState,
    snackbarHostState: SnackbarHostState,
    actions: PaywallActions,
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
                premiumFeatures = (uiState as? PaywallUiState.Success)?.premiumFeatures.orEmpty(),
                dimensions = dimensions,
                onBackClick = actions.onBackClick,
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
                    text = stringResource(R.string.paywall_choose_your_plan),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = dimensions.headerTitleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                PaywallLandscapePlanSection(
                    uiState = uiState,
                    actions = actions,
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
                        onRestoreClick = actions.onRestoreClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaywallLandscapePlanSection(
    uiState: PaywallUiState,
    actions: PaywallActions,
    dimensions: PaywallLandscapeDimensions,
) {
    val contentModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = dimensions.contentPaddingHorizontal)
    when (uiState) {
        is PaywallUiState.Success -> {
            SubscriptionPlanRow(
                modifier = contentModifier,
                subscriptionPlans = uiState.subscriptionPlans,
                onPlanSelected = actions.onPlanSelected,
                itemSpacing = dimensions.plansSpacing,
                priceFontSize = dimensions.planPriceFontSize,
                priceUnitFontSize = dimensions.planPriceUnitFontSize,
            )
            StartProButton(
                modifier = contentModifier
                    .padding(top = dimensions.actionButtonTopPadding)
                    .height(dimensions.actionButtonHeight),
                isLoading = uiState.isPurchasing,
                priceDescription = selectedPlanPriceText(uiState),
                onClick = actions.onStartProClick,
            )
        }

        PaywallUiState.Loading -> PaywallLoadingIndicator(modifier = contentModifier)

        PaywallUiState.Error -> PaywallErrorCard(modifier = contentModifier)
    }
}
