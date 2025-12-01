package com.quare.bibleplanner.core.provider.koin

import com.quare.bibleplanner.core.books.di.booksModule
import com.quare.bibleplanner.core.datastore.di.dataStoreProviderModule
import com.quare.bibleplanner.core.plan.di.planModule
import com.quare.bibleplanner.core.provider.room.di.roomModule
import com.quare.bibleplanner.feature.day.di.dayModule
import com.quare.bibleplanner.feature.readingplan.di.readingPlanModule
import com.quare.bibleplanner.feature.themeselection.di.themeSelectionDomainModule

object CommonKoinUtils {
    val modules = listOf(
        booksModule,
        planModule,
        dataStoreProviderModule,
        themeSelectionDomainModule,
        readingPlanModule,
        dayModule,
        roomModule,
    )
}
