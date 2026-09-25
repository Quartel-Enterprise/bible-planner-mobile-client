package com.quare.bibleplanner.feature.readingplan.presentation.factory

import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.delete_progress_option
import bibleplanner.feature.reading_plan.generated.resources.edit_plan_start_day
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOption
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOptionPresentationModel
import com.quare.bibleplanner.ui.icons.AppIcon

internal object ReadingPlanMenuOptionsFactory {
    val options = listOf(
        OverflowOptionPresentationModel(
            name = Res.string.edit_plan_start_day,
            type = OverflowOption.EDIT_START_DAY,
            icon = AppIcon.Edit,
        ),
        OverflowOptionPresentationModel(
            name = Res.string.delete_progress_option,
            type = OverflowOption.DELETE_PROGRESS,
            icon = AppIcon.Delete,
        ),
    )
}
