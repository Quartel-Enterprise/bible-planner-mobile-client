package com.quare.bibleplanner.feature.paywall.presentation.utils

import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.feature_ad_free
import bibleplanner.feature.paywall.generated.resources.feature_ad_free_subtext
import bibleplanner.feature.paywall.generated.resources.feature_ai_chapter_study
import bibleplanner.feature.paywall.generated.resources.feature_ai_chapter_study_subtext
import bibleplanner.feature.paywall.generated.resources.feature_ai_chat
import bibleplanner.feature.paywall.generated.resources.feature_ai_chat_subtext
import bibleplanner.feature.paywall.generated.resources.feature_ai_day_study
import bibleplanner.feature.paywall.generated.resources.feature_ai_day_study_subtext
import bibleplanner.feature.paywall.generated.resources.feature_chapter_listening
import bibleplanner.feature.paywall.generated.resources.feature_chapter_listening_subtext
import bibleplanner.feature.paywall.generated.resources.feature_highlight_colors
import bibleplanner.feature.paywall.generated.resources.feature_highlight_colors_subtext
import bibleplanner.feature.paywall.generated.resources.feature_share_image_style
import bibleplanner.feature.paywall.generated.resources.feature_share_image_style_subtext
import bibleplanner.feature.paywall.generated.resources.feature_unlimited_notes
import bibleplanner.feature.paywall.generated.resources.feature_unlimited_notes_subtext
import com.quare.bibleplanner.feature.paywall.presentation.model.FixedPremiumFeature
import com.quare.bibleplanner.feature.paywall.presentation.model.UnlimitedNotesPremiumFeature
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PremiumFeatureTextTest {
    @Test
    fun `GIVEN each fixed feature WHEN mapping its texts THEN picks its title and subtext`() {
        // Given
        val features = FixedPremiumFeature.entries

        // When
        val texts = features.associateWith { feature -> feature.toTitleResource() to feature.toSubtextResource() }

        // Then
        assertEquals(
            mapOf(
                FixedPremiumFeature.AI_DAY_STUDY to
                    (Res.string.feature_ai_day_study to Res.string.feature_ai_day_study_subtext),
                FixedPremiumFeature.AI_CHAPTER_STUDY to
                    (Res.string.feature_ai_chapter_study to Res.string.feature_ai_chapter_study_subtext),
                FixedPremiumFeature.AI_CHAT to (Res.string.feature_ai_chat to Res.string.feature_ai_chat_subtext),
                FixedPremiumFeature.CHAPTER_LISTENING to
                    (Res.string.feature_chapter_listening to Res.string.feature_chapter_listening_subtext),
                FixedPremiumFeature.AD_FREE to (Res.string.feature_ad_free to Res.string.feature_ad_free_subtext),
                FixedPremiumFeature.HIGHLIGHT_COLORS to
                    (Res.string.feature_highlight_colors to Res.string.feature_highlight_colors_subtext),
                FixedPremiumFeature.SHARE_IMAGE_STYLE to
                    (Res.string.feature_share_image_style to Res.string.feature_share_image_style_subtext),
            ),
            texts,
        )
    }

    @Test
    fun `GIVEN the unlimited notes feature WHEN mapping its texts THEN maps title subtext and limit`() {
        // Given
        val feature = UnlimitedNotesPremiumFeature(maxFreeNotes = 3)

        // When
        val texts = Triple(feature.toTitleResource(), feature.toSubtextResource(), feature.toSubtextArgs())

        // Then
        assertEquals(
            Triple(Res.string.feature_unlimited_notes, Res.string.feature_unlimited_notes_subtext, listOf<Any>(3)),
            texts,
        )
    }

    @Test
    fun `GIVEN a fixed feature WHEN reading its subtext args THEN has none`() {
        // Given
        val feature = FixedPremiumFeature.AD_FREE

        // When
        val args = feature.toSubtextArgs()

        // Then
        assertEquals(emptyList(), args)
    }
}
