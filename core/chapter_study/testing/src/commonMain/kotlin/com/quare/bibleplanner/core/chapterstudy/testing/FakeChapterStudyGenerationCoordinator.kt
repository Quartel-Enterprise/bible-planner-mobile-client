package com.quare.bibleplanner.core.chapterstudy.testing

import com.quare.bibleplanner.core.chapterstudy.domain.coordinator.ChapterStudyGenerationCoordinator
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationJob
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeChapterStudyGenerationCoordinator : ChapterStudyGenerationCoordinator {
    val jobsFlow = MutableStateFlow<List<ChapterStudyGenerationJob>>(emptyList())
    val startedTargets = mutableListOf<ChapterStudyTargetModel>()
    val acknowledgedTargets = mutableListOf<ChapterStudyTargetModel>()
    val generatingCountExclusions = mutableListOf<ChapterStudyTargetModel>()
    var generatingCount = 0

    override val jobs: StateFlow<List<ChapterStudyGenerationJob>> = jobsFlow

    override fun start(target: ChapterStudyTargetModel) {
        startedTargets += target
    }

    override fun acknowledge(target: ChapterStudyTargetModel) {
        acknowledgedTargets += target
        jobsFlow.value = jobsFlow.value.filterNot { it.target == target }
    }

    override fun getGeneratingCount(excluding: ChapterStudyTargetModel): Int {
        generatingCountExclusions += excluding
        return generatingCount
    }
}
