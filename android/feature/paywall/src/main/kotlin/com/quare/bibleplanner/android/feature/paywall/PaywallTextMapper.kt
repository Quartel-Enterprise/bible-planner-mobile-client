package com.quare.bibleplanner.android.feature.paywall

import androidx.annotation.StringRes
import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType
import com.quare.bibleplanner.feature.paywall.presentation.model.FixedPremiumFeature
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallError
import com.quare.bibleplanner.feature.paywall.presentation.model.PremiumFeature
import com.quare.bibleplanner.feature.paywall.presentation.model.UnlimitedNotesPremiumFeature

@StringRes
internal fun PaywallError.toMessage(): Int = when (this) {
    PaywallError.PURCHASE_CANCELLED -> R.string.paywall_error_purchase_cancelled
    PaywallError.NETWORK -> R.string.paywall_error_network
    PaywallError.PAYMENT_PENDING -> R.string.paywall_error_payment_pending
    PaywallError.RESTORE_PURCHASE_FAILED -> R.string.paywall_error_restore_purchase
    PaywallError.BROWSER_CHECKOUT_NOT_CONFIRMED -> R.string.paywall_error_browser_checkout_not_confirmed
    PaywallError.UNKNOWN -> R.string.paywall_error_unknown
}

@StringRes
internal fun PremiumFeature.toTitle(): Int = when (this) {
    FixedPremiumFeature.AI_DAY_STUDY -> R.string.paywall_feature_ai_day_study
    FixedPremiumFeature.AI_CHAPTER_STUDY -> R.string.paywall_feature_ai_chapter_study
    FixedPremiumFeature.AI_CHAT -> R.string.paywall_feature_ai_chat
    FixedPremiumFeature.CHAPTER_LISTENING -> R.string.paywall_feature_chapter_listening
    FixedPremiumFeature.AD_FREE -> R.string.paywall_feature_ad_free
    FixedPremiumFeature.HIGHLIGHT_COLORS -> R.string.paywall_feature_highlight_colors
    FixedPremiumFeature.SHARE_IMAGE_STYLE -> R.string.paywall_feature_share_image_style
    is UnlimitedNotesPremiumFeature -> R.string.paywall_feature_unlimited_notes
}

@StringRes
internal fun PremiumFeature.toSubtext(): Int = when (this) {
    FixedPremiumFeature.AI_DAY_STUDY -> R.string.paywall_feature_ai_day_study_subtext
    FixedPremiumFeature.AI_CHAPTER_STUDY -> R.string.paywall_feature_ai_chapter_study_subtext
    FixedPremiumFeature.AI_CHAT -> R.string.paywall_feature_ai_chat_subtext
    FixedPremiumFeature.CHAPTER_LISTENING -> R.string.paywall_feature_chapter_listening_subtext
    FixedPremiumFeature.AD_FREE -> R.string.paywall_feature_ad_free_subtext
    FixedPremiumFeature.HIGHLIGHT_COLORS -> R.string.paywall_feature_highlight_colors_subtext
    FixedPremiumFeature.SHARE_IMAGE_STYLE -> R.string.paywall_feature_share_image_style_subtext
    is UnlimitedNotesPremiumFeature -> R.string.paywall_feature_unlimited_notes_subtext
}

internal fun PremiumFeature.toSubtextArgs(): Array<Any> = when (this) {
    is FixedPremiumFeature -> emptyArray()
    is UnlimitedNotesPremiumFeature -> arrayOf(maxFreeNotes)
}

@StringRes
internal fun SubscriptionPlanType.toTitle(): Int = when (this) {
    SubscriptionPlanType.Monthly -> R.string.paywall_plan_monthly
    SubscriptionPlanType.Annual -> R.string.paywall_plan_annual
}

@StringRes
internal fun SubscriptionPlanType.toDescription(): Int = when (this) {
    SubscriptionPlanType.Monthly -> R.string.paywall_plan_monthly_description
    SubscriptionPlanType.Annual -> R.string.paywall_plan_annual_description
}

@StringRes
internal fun SubscriptionPlanType.toPeriod(): Int = when (this) {
    SubscriptionPlanType.Monthly -> R.string.paywall_per_month
    SubscriptionPlanType.Annual -> R.string.paywall_per_year
}

@StringRes
internal fun SubscriptionPlanType.toPeriodUnit(): Int = when (this) {
    SubscriptionPlanType.Monthly -> R.string.paywall_month
    SubscriptionPlanType.Annual -> R.string.paywall_year
}
