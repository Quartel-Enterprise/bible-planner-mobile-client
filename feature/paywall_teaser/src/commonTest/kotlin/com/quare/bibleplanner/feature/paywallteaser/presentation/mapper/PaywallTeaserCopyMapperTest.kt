package com.quare.bibleplanner.feature.paywallteaser.presentation.mapper

import bibleplanner.feature.paywall_teaser.generated.resources.Res
import bibleplanner.feature.paywall_teaser.generated.resources.paywall_teaser_chapter_study_limit_body
import bibleplanner.feature.paywall_teaser.generated.resources.paywall_teaser_chapter_study_limit_dismiss
import bibleplanner.feature.paywall_teaser.generated.resources.paywall_teaser_chapter_study_limit_title
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
        // Given
        val reason = PaywallTeaserReason.HIGHLIGHT_CUSTOM_COLOR

        // When
        val copy = reason.toCopy()

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

    @Test
    fun `GIVEN the chapter study limit reason WHEN mapping it to the copy THEN explains the used up studies`() {
        // Given
        val reason = PaywallTeaserReason.CHAPTER_STUDY_LIMIT

        // When
        val copy = reason.toCopy()

        // Then
        assertEquals(
            PaywallTeaserCopy(
                title = Res.string.paywall_teaser_chapter_study_limit_title,
                body = Res.string.paywall_teaser_chapter_study_limit_body,
                dismiss = Res.string.paywall_teaser_chapter_study_limit_dismiss,
            ),
            copy,
        )
    }
}
