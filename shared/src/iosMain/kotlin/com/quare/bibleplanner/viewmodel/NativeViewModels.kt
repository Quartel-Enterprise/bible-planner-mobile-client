package com.quare.bibleplanner.viewmodel

import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.presentation.bridge.ViewModelHolder
import com.quare.bibleplanner.feature.books.presentation.viewmodel.BooksViewModel
import com.quare.bibleplanner.feature.day.presentation.viewmodel.DayViewModel
import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel
import com.quare.bibleplanner.feature.read.presentation.ReadViewModel
import com.quare.bibleplanner.feature.readingplan.presentation.viewmodel.ReadingPlanViewModel
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform

/*
 * Why: the composition root builds each SwiftUI screen's ViewModelHolder, so Swift never passes a KClass nor
 * reaches Koin. Each screen keeps its holder and clears it when the screen goes away.
 */
object NativeViewModels {
    // Why: Koin throws when it isn't started or can't build the ViewModel; without @Throws that terminates the app.
    @Throws(Exception::class)
    fun createReadingPlan(): ViewModelHolder<ReadingPlanViewModel> = ViewModelHolder(ReadingPlanViewModel::class) {
        KoinPlatform.getKoin().get()
    }

    @Throws(Exception::class)
    fun createBooks(): ViewModelHolder<BooksViewModel> = ViewModelHolder(BooksViewModel::class) {
        KoinPlatform.getKoin().get()
    }

    @Throws(Exception::class)
    fun createRead(route: ReadNavRoute): ViewModelHolder<ReadViewModel> = ViewModelHolder(ReadViewModel::class) {
        KoinPlatform.getKoin().get { parametersOf(route) }
    }

    @Throws(Exception::class)
    fun createMain(): ViewModelHolder<MainScreenViewModel> = ViewModelHolder(MainScreenViewModel::class) {
        KoinPlatform.getKoin().get()
    }

    @Throws(Exception::class)
    fun createDay(route: DayNavRoute): ViewModelHolder<DayViewModel> = ViewModelHolder(DayViewModel::class) {
        KoinPlatform.getKoin().get { parametersOf(route) }
    }
}
