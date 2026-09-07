package com.quare.bibleplanner.feature.paywall.presentation.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class PaywallLandscapeDimensions(
    val panelPaddingStart: Dp,
    val panelPaddingTop: Dp,
    val panelPaddingEnd: Dp,
    val panelPaddingBottom: Dp,
    val panelContentStartPadding: Dp,
    val heroIcon: HeroIcon?,
    val heroTitleFontSize: TextUnit,
    val heroBottomSpacing: Int,
    val featureSpacing: Dp,
    val featureIconSize: Dp,
    val contentPaddingHorizontal: Dp,
    val contentPaddingTop: Dp,
    val contentPaddingBottom: Dp,
    val headerTitleFontSize: TextUnit,
    val headerBottomPadding: Dp,
    val centersPlanBlock: Boolean,
    val plansSpacing: Dp,
    val planPriceFontSize: TextUnit,
    val planPriceUnitFontSize: TextUnit,
    val actionButtonTopPadding: Dp,
    val actionButtonHeight: Dp,
) {
    /**
     * Configuration of the premium icon above the panel title. Absent on a phone in landscape,
     * where the panel opens with a compact bar instead of the full hero.
     */
    data class HeroIcon(
        val boxSize: Dp,
        val boxCornerRadius: Dp,
        val iconSize: Dp,
    )

    companion object {
        val Compact = PaywallLandscapeDimensions(
            panelPaddingStart = 12.dp,
            panelPaddingTop = 14.dp,
            panelPaddingEnd = 28.dp,
            panelPaddingBottom = 22.dp,
            panelContentStartPadding = 8.dp,
            heroIcon = null,
            heroTitleFontSize = 20.sp,
            heroBottomSpacing = 14,
            featureSpacing = 10.dp,
            featureIconSize = 22.dp,
            contentPaddingHorizontal = 26.dp,
            contentPaddingTop = 24.dp,
            contentPaddingBottom = 18.dp,
            headerTitleFontSize = 17.sp,
            headerBottomPadding = 14.dp,
            centersPlanBlock = false,
            plansSpacing = 12.dp,
            planPriceFontSize = 22.sp,
            planPriceUnitFontSize = 12.sp,
            actionButtonTopPadding = 14.dp,
            actionButtonHeight = 48.dp,
        )

        val Regular = PaywallLandscapeDimensions(
            panelPaddingStart = 24.dp,
            panelPaddingTop = 20.dp,
            panelPaddingEnd = 44.dp,
            panelPaddingBottom = 44.dp,
            panelContentStartPadding = 20.dp,
            heroIcon = HeroIcon(
                boxSize = 60.dp,
                boxCornerRadius = 18.dp,
                iconSize = 32.dp,
            ),
            heroTitleFontSize = 36.sp,
            heroBottomSpacing = 30,
            featureSpacing = 20.dp,
            featureIconSize = 28.dp,
            contentPaddingHorizontal = 44.dp,
            contentPaddingTop = 0.dp,
            contentPaddingBottom = 26.dp,
            headerTitleFontSize = 21.sp,
            headerBottomPadding = 18.dp,
            centersPlanBlock = true,
            plansSpacing = 14.dp,
            planPriceFontSize = 28.sp,
            planPriceUnitFontSize = 13.sp,
            actionButtonTopPadding = 18.dp,
            actionButtonHeight = 54.dp,
        )
    }
}
