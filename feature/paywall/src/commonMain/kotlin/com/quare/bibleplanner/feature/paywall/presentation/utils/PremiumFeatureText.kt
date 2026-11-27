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
import com.quare.bibleplanner.feature.paywall.presentation.model.PremiumFeature
import com.quare.bibleplanner.feature.paywall.presentation.model.UnlimitedNotesPremiumFeature
import org.jetbrains.compose.resources.StringResource

internal fun PremiumFeature.toTitleResource(): StringResource = when (this) {
    FixedPremiumFeature.AI_DAY_STUDY -> Res.string.feature_ai_day_study
    FixedPremiumFeature.AI_CHAPTER_STUDY -> Res.string.feature_ai_chapter_study
    FixedPremiumFeature.AI_CHAT -> Res.string.feature_ai_chat
    FixedPremiumFeature.CHAPTER_LISTENING -> Res.string.feature_chapter_listening
    FixedPremiumFeature.AD_FREE -> Res.string.feature_ad_free
    FixedPremiumFeature.HIGHLIGHT_COLORS -> Res.string.feature_highlight_colors
    FixedPremiumFeature.SHARE_IMAGE_STYLE -> Res.string.feature_share_image_style
    is UnlimitedNotesPremiumFeature -> Res.string.feature_unlimited_notes
}

internal fun PremiumFeature.toSubtextResource(): StringResource = when (this) {
    FixedPremiumFeature.AI_DAY_STUDY -> Res.string.feature_ai_day_study_subtext
    FixedPremiumFeature.AI_CHAPTER_STUDY -> Res.string.feature_ai_chapter_study_subtext
    FixedPremiumFeature.AI_CHAT -> Res.string.feature_ai_chat_subtext
    FixedPremiumFeature.CHAPTER_LISTENING -> Res.string.feature_chapter_listening_subtext
    FixedPremiumFeature.AD_FREE -> Res.string.feature_ad_free_subtext
    FixedPremiumFeature.HIGHLIGHT_COLORS -> Res.string.feature_highlight_colors_subtext
    FixedPremiumFeature.SHARE_IMAGE_STYLE -> Res.string.feature_share_image_style_subtext
    is UnlimitedNotesPremiumFeature -> Res.string.feature_unlimited_notes_subtext
}

internal fun PremiumFeature.toSubtextArgs(): List<Any> = when (this) {
    is FixedPremiumFeature -> emptyList()
    is UnlimitedNotesPremiumFeature -> listOf(maxFreeNotes)
}
