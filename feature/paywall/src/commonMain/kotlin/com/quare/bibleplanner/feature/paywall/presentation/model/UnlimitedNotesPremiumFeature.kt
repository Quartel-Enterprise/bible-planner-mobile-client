package com.quare.bibleplanner.feature.paywall.presentation.model

import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.feature_unlimited_notes
import bibleplanner.feature.paywall.generated.resources.feature_unlimited_notes_subtext
import org.jetbrains.compose.resources.StringResource

data class UnlimitedNotesPremiumFeature(
    val maxFreeNotes: Int,
) : PremiumFeature {
    override val title: StringResource = Res.string.feature_unlimited_notes
    override val subtext: StringResource = Res.string.feature_unlimited_notes_subtext
}
