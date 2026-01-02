package com.quare.bibleplanner.core.provider.koin

import com.quare.bibleplanner.core.books.di.booksModule
import com.quare.bibleplanner.core.datastore.di.dataStoreProviderModule
import com.quare.bibleplanner.core.date.di.dateModule
import com.quare.bibleplanner.core.plan.di.planModule
import com.quare.bibleplanner.core.provider.platform.di.platformModule
import com.quare.bibleplanner.core.provider.room.di.roomModule
import com.quare.bibleplanner.core.utils.di.utilsModule
import com.quare.bibleplanner.feature.day.di.dayModule
import com.quare.bibleplanner.feature.deletenotes.di.deleteNotesModule
import com.quare.bibleplanner.feature.deleteprogress.di.deleteProgressModule
import com.quare.bibleplanner.feature.editplanstartdate.di.editPlanStartDateModule
import com.quare.bibleplanner.feature.materialyou.di.materialYouModule
import com.quare.bibleplanner.feature.onboardingstartdate.di.onboardingStartDateModule
import com.quare.bibleplanner.feature.readingplan.di.readingPlanModule
import com.quare.bibleplanner.feature.themeselection.di.themeSelectionDomainModule
import com.quare.bibleplanner.feature.unlockpremium.di.unlockPremiumModule

object CommonKoinUtils {
    val modules = listOf(
        booksModule,
        planModule,
        platformModule,
        dataStoreProviderModule,
        themeSelectionDomainModule,
        materialYouModule,
        readingPlanModule,
        dayModule,
        deleteProgressModule,
        deleteNotesModule,
        editPlanStartDateModule,
        onboardingStartDateModule,
        unlockPremiumModule,
        roomModule,
        utilsModule,
        dateModule,
    )
}
