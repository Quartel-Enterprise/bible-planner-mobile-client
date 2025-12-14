package com.quare.bibleplanner.feature.readingplan.presentation.factory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.delete_progress_option
import bibleplanner.feature.reading_plan.generated.resources.privacy_policy
import bibleplanner.feature.reading_plan.generated.resources.terms_of_service
import bibleplanner.feature.reading_plan.generated.resources.theme_option
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOption
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOptionPresentationModel

internal object ReadingPlanMenuOptionsFactory {
    val options = listOf(
        OverflowOptionPresentationModel(
            name = Res.string.theme_option,
            type = OverflowOption.THEME,
            icon = Icons.Default.Palette
        ),
        OverflowOptionPresentationModel(
            name = Res.string.terms_of_service,
            type = OverflowOption.TERMS,
            icon = Icons.Default.Info,
        ),
        OverflowOptionPresentationModel(
            name = Res.string.privacy_policy,
            type = OverflowOption.PRIVACY_POLICY,
            icon = Icons.Default.Shield,
        ),
        OverflowOptionPresentationModel(
            name = Res.string.delete_progress_option,
            type = OverflowOption.DELETE_PROGRESS,
            icon = Icons.Default.Delete,
        )
    )
}
