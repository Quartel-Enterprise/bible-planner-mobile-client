package com.quare.bibleplanner.core.provider.koin

import com.quare.bibleplanner.core.books.di.booksModule
import com.quare.bibleplanner.core.datastore.di.dataStoreProviderModule
import com.quare.bibleplanner.core.date.di.dateModule
import com.quare.bibleplanner.core.network.data.di.networkModule
import com.quare.bibleplanner.core.plan.di.planModule
import com.quare.bibleplanner.core.provider.billing.di.billingProviderModule
import com.quare.bibleplanner.core.provider.platform.di.platformModule
import com.quare.bibleplanner.core.provider.room.di.roomModule
import com.quare.bibleplanner.core.remoteconfig.di.remoteConfigModule
import com.quare.bibleplanner.core.utils.di.utilsModule
import com.quare.bibleplanner.core.utils.jsonreader.di.jsonReaderModule
import com.quare.bibleplanner.feature.addnotesfreewarning.di.addNotesFreeWarningModule
import com.quare.bibleplanner.feature.bookdetails.di.bookDetailsModule
import com.quare.bibleplanner.feature.books.di.featureBooksModule
import com.quare.bibleplanner.feature.congrats.di.congratsModule
import com.quare.bibleplanner.feature.day.di.dayModule
import com.quare.bibleplanner.feature.deletenotes.di.deleteNotesModule
import com.quare.bibleplanner.feature.deleteprogress.di.deleteProgressModule
import com.quare.bibleplanner.feature.donation.di.donationModule
import com.quare.bibleplanner.feature.donation.pixqr.di.pixQrModule
import com.quare.bibleplanner.feature.editplanstartdate.di.editPlanStartDateModule
import com.quare.bibleplanner.feature.login.di.loginModule
import com.quare.bibleplanner.feature.main.di.mainModule
import com.quare.bibleplanner.feature.materialyou.di.materialYouModule
import com.quare.bibleplanner.feature.more.di.moreModule
import com.quare.bibleplanner.feature.paywall.di.paywallModule
import com.quare.bibleplanner.feature.readingplan.di.readingPlanModule
import com.quare.bibleplanner.feature.releasenotes.di.releaseNotesModule
import com.quare.bibleplanner.feature.subscriptiondetails.di.subscriptionDetailsModule
import com.quare.bibleplanner.feature.themeselection.di.themeSelectionDomainModule

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
        addNotesFreeWarningModule,
        editPlanStartDateModule,
        paywallModule,
        roomModule,
        utilsModule,
        jsonReaderModule,
        dateModule,
        remoteConfigModule,
        billingProviderModule,
        congratsModule,
        mainModule,
        moreModule,
        featureBooksModule,
        subscriptionDetailsModule,
        releaseNotesModule,
        donationModule,
        networkModule,
        pixQrModule,
        bookDetailsModule,
        loginModule,
    )
}
