package com.quare.bibleplanner.feature.daystudy.presentation.component

import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_generate
import bibleplanner.feature.day_study.generated.resources.ai_study_subscribe
import bibleplanner.feature.day_study.generated.resources.ai_study_view
import bibleplanner.ui.component.generated.resources.ai_study_unlock
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import org.jetbrains.compose.resources.StringResource
import bibleplanner.ui.component.generated.resources.Res as ComponentRes

internal fun DayStudyCardUiModel.toButtonLabel(): StringResource = when (mode) {
    null, DayStudyCardMode.GENERATE -> Res.string.ai_study_generate

    DayStudyCardMode.VIEW -> Res.string.ai_study_view

    DayStudyCardMode.LOCKED -> if (isRewardedUnlockOffered) {
        ComponentRes.string.ai_study_unlock
    } else {
        Res.string.ai_study_subscribe
    }
}
