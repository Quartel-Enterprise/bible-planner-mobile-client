package com.quare.bibleplanner.feature.paywall.presentation.model

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
import org.jetbrains.compose.resources.StringResource

enum class FixedPremiumFeature(
    override val title: StringResource,
    val subtext: StringResource,
) : PremiumFeature {
    AI_DAY_STUDY(
        title = Res.string.feature_ai_day_study,
        subtext = Res.string.feature_ai_day_study_subtext,
    ),
    AI_CHAPTER_STUDY(
        title = Res.string.feature_ai_chapter_study,
        subtext = Res.string.feature_ai_chapter_study_subtext,
    ),
    AI_CHAT(
        title = Res.string.feature_ai_chat,
        subtext = Res.string.feature_ai_chat_subtext,
    ),
    CHAPTER_LISTENING(
        title = Res.string.feature_chapter_listening,
        subtext = Res.string.feature_chapter_listening_subtext,
    ),
    AD_FREE(
        title = Res.string.feature_ad_free,
        subtext = Res.string.feature_ad_free_subtext,
    ),
    HIGHLIGHT_COLORS(
        title = Res.string.feature_highlight_colors,
        subtext = Res.string.feature_highlight_colors_subtext,
    ),
    SHARE_IMAGE_STYLE(
        title = Res.string.feature_share_image_style,
        subtext = Res.string.feature_share_image_style_subtext,
    ),
}
