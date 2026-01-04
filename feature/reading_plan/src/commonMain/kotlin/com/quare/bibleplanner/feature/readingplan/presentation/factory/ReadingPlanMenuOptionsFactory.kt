package com.quare.bibleplanner.feature.readingplan.presentation.factory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.delete_progress_option
import bibleplanner.feature.reading_plan.generated.resources.edit_plan_start_day
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOption
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOptionPresentationModel

internal object ReadingPlanMenuOptionsFactory {
    val options = listOf(
        OverflowOptionPresentationModel(
            name = Res.string.edit_plan_start_day,
            type = OverflowOption.EDIT_START_DAY,
            icon = Icons.Default.Edit,
        ),
        OverflowOptionPresentationModel(
            name = Res.string.delete_progress_option,
            type = OverflowOption.DELETE_PROGRESS,
            icon = Icons.Default.Delete,
        ),
    )
}
