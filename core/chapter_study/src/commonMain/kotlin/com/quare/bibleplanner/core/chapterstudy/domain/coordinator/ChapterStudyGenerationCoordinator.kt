package com.quare.bibleplanner.core.chapterstudy.domain.coordinator

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationJob
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import kotlinx.coroutines.flow.StateFlow

interface ChapterStudyGenerationCoordinator {
    val jobs: StateFlow<List<ChapterStudyGenerationJob>>

    fun start(
        target: ChapterStudyTargetModel,
        isRewarded: Boolean,
    )

    fun acknowledge(target: ChapterStudyTargetModel)

    fun getGeneratingCount(excluding: ChapterStudyTargetModel): Int

    fun hasUnservedReward(target: ChapterStudyTargetModel): Boolean
}
