package com.quare.bibleplanner.android.feature.paywallteaser

import com.quare.bibleplanner.core.model.route.PaywallTeaserReason

internal fun PaywallTeaserReason.toCopy(): PaywallTeaserCopy = when (this) {
    PaywallTeaserReason.HIGHLIGHT_CUSTOM_COLOR -> PaywallTeaserCopy(
        title = R.string.paywall_teaser_highlight_custom_color_title,
        body = R.string.paywall_teaser_highlight_custom_color_body,
        dismiss = R.string.paywall_teaser_highlight_custom_color_dismiss,
    )

    PaywallTeaserReason.CHAPTER_STUDY_LIMIT -> PaywallTeaserCopy(
        title = R.string.paywall_teaser_chapter_study_limit_title,
        body = R.string.paywall_teaser_chapter_study_limit_body,
        dismiss = R.string.paywall_teaser_chapter_study_limit_dismiss,
    )

    PaywallTeaserReason.CHAPTER_LISTENING_LIMIT -> PaywallTeaserCopy(
        title = R.string.paywall_teaser_chapter_listening_limit_title,
        body = R.string.paywall_teaser_chapter_listening_limit_body,
        dismiss = R.string.paywall_teaser_chapter_listening_limit_dismiss,
    )

    PaywallTeaserReason.CHAPTER_LISTENING_FREE_LIMIT -> PaywallTeaserCopy(
        title = R.string.paywall_teaser_chapter_listening_limit_title,
        body = R.string.paywall_teaser_chapter_listening_limit_body_free,
        dismiss = R.string.paywall_teaser_chapter_listening_limit_dismiss,
    )

    PaywallTeaserReason.CHAPTER_LISTENING_PRO_ONLY -> PaywallTeaserCopy(
        title = R.string.paywall_teaser_chapter_listening_pro_only_title,
        body = R.string.paywall_teaser_chapter_listening_pro_only_body,
        dismiss = R.string.paywall_teaser_chapter_listening_limit_dismiss,
    )

    PaywallTeaserReason.SHARE_IMAGE_STYLE -> PaywallTeaserCopy(
        title = R.string.paywall_teaser_share_image_style_title,
        body = R.string.paywall_teaser_share_image_style_body,
        dismiss = R.string.paywall_teaser_share_image_style_dismiss,
    )
}
