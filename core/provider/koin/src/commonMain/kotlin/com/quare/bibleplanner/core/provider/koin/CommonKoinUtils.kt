package com.quare.bibleplanner.core.provider.koin

import com.quare.bibleplanner.core.books.di.booksModule
import com.quare.bibleplanner.core.plan.di.planModule
import com.quare.bibleplanner.core.provider.room.di.roomModule
import com.quare.bibleplanner.feature.readingplan.di.readingPlanModule

object CommonKoinUtils {
    val modules = listOf(
        booksModule,
        planModule,
        readingPlanModule,
        roomModule,
    )
}
