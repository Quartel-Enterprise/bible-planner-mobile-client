package com.quare.bibleplanner.feature.paywall.presentation.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class PaywallLandscapeDimensions(
    val panelPaddingHorizontal: Dp,
    val panelPaddingVertical: Dp,
    val heroIconBoxSize: Dp,
    val heroIconBoxCornerRadius: Dp,
    val heroIconSize: Dp,
    val heroTitleFontSize: TextUnit,
    val heroBottomSpacing: Int,
    val featureSpacing: Dp,
    val featureIconSize: Dp,
    val contentPaddingHorizontal: Dp,
    val contentPaddingTop: Dp,
    val contentPaddingBottom: Dp,
    val headerTitleFontSize: TextUnit,
    val closeButtonSize: Dp,
    val closeIconSize: Dp,
    val plansSpacing: Dp,
    val actionButtonHeight: Dp,
) {
    companion object {
        val Compact = PaywallLandscapeDimensions(
            panelPaddingHorizontal = 28.dp,
            panelPaddingVertical = 26.dp,
            heroIconBoxSize = 46.dp,
            heroIconBoxCornerRadius = 14.dp,
            heroIconSize = 26.dp,
            heroTitleFontSize = 26.sp,
            heroBottomSpacing = 20,
            featureSpacing = 14.dp,
            featureIconSize = 24.dp,
            contentPaddingHorizontal = 26.dp,
            contentPaddingTop = 22.dp,
            contentPaddingBottom = 20.dp,
            headerTitleFontSize = 17.sp,
            closeButtonSize = 34.dp,
            closeIconSize = 18.dp,
            plansSpacing = 12.dp,
            actionButtonHeight = 48.dp,
        )

        val Regular = PaywallLandscapeDimensions(
            panelPaddingHorizontal = 44.dp,
            panelPaddingVertical = 52.dp,
            heroIconBoxSize = 60.dp,
            heroIconBoxCornerRadius = 18.dp,
            heroIconSize = 32.dp,
            heroTitleFontSize = 36.sp,
            heroBottomSpacing = 34,
            featureSpacing = 22.dp,
            featureIconSize = 30.dp,
            contentPaddingHorizontal = 44.dp,
            contentPaddingTop = 40.dp,
            contentPaddingBottom = 26.dp,
            headerTitleFontSize = 21.sp,
            closeButtonSize = 40.dp,
            closeIconSize = 20.dp,
            plansSpacing = 14.dp,
            actionButtonHeight = 56.dp,
        )
    }
}
