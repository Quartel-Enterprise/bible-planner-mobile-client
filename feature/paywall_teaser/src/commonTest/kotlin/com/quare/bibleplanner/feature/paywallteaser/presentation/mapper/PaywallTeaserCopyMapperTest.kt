package com.quare.bibleplanner.feature.paywallteaser.presentation.mapper

import bibleplanner.feature.paywall_teaser.generated.resources.Res
import bibleplanner.feature.paywall_teaser.generated.resources.paywall_teaser_highlight_custom_color_body
import bibleplanner.feature.paywall_teaser.generated.resources.paywall_teaser_highlight_custom_color_dismiss
import bibleplanner.feature.paywall_teaser.generated.resources.paywall_teaser_highlight_custom_color_title
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.feature.paywallteaser.presentation.model.PaywallTeaserCopy
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PaywallTeaserCopyMapperTest {
    @Test
    fun `GIVEN the custom highlight color reason WHEN mapping it to the copy THEN explains the custom colors`() {
        // When
        val copy = PaywallTeaserReason.HIGHLIGHT_CUSTOM_COLOR.toCopy()

        // Then
        assertEquals(
            PaywallTeaserCopy(
                title = Res.string.paywall_teaser_highlight_custom_color_title,
                body = Res.string.paywall_teaser_highlight_custom_color_body,
                dismiss = Res.string.paywall_teaser_highlight_custom_color_dismiss,
            ),
            copy,
        )
    }
}
