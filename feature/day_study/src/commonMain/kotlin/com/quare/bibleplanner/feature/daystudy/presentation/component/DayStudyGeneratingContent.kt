package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_generating_subtitle
import bibleplanner.feature.day_study.generated.resources.ai_study_phase_chapters
import bibleplanner.feature.day_study.generated.resources.ai_study_phase_context
import bibleplanner.feature.day_study.generated.resources.ai_study_phase_questions
import bibleplanner.feature.day_study.generated.resources.ai_study_phase_reading
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationPhase
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.ui.component.study.AiStudyGeneratingContent
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DayStudyGeneratingContent(
    generation: DayStudyGenerationUiModel,
    modifier: Modifier = Modifier,
) {
    AiStudyGeneratingContent(
        subtitle = stringResource(Res.string.ai_study_generating_subtitle),
        phases = DayStudyGenerationPhase.entries.map { phase -> stringResource(phase.titleRes) },
        currentPhaseIndex = generation.currentPhaseIndex,
        modifier = modifier,
    )
}

internal val DayStudyGenerationPhase.titleRes: StringResource
    get() = when (this) {
        DayStudyGenerationPhase.READING -> Res.string.ai_study_phase_reading
        DayStudyGenerationPhase.CHAPTERS -> Res.string.ai_study_phase_chapters
        DayStudyGenerationPhase.CONTEXT -> Res.string.ai_study_phase_context
        DayStudyGenerationPhase.QUESTIONS -> Res.string.ai_study_phase_questions
    }
