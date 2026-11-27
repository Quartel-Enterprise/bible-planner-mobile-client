package com.quare.bibleplanner.viewmodel

import com.quare.bibleplanner.core.presentation.bridge.ViewModelHolder
import com.quare.bibleplanner.feature.readingplan.presentation.viewmodel.ReadingPlanViewModel
import org.koin.mp.KoinPlatform

/*
 * Why: the composition root builds each SwiftUI screen's ViewModelHolder, so Swift never passes a KClass nor
 * reaches Koin. Each screen keeps its holder and clears it when the screen goes away.
 */
object NativeViewModels {
    fun createReadingPlan(): ViewModelHolder<ReadingPlanViewModel> = ViewModelHolder(ReadingPlanViewModel::class) {
        KoinPlatform.getKoin().get()
    }
}
