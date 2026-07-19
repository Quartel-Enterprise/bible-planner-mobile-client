package com.quare.bibleplanner.core.provider.koin

import com.quare.bibleplanner.core.books.di.booksModule
import com.quare.bibleplanner.core.clear.di.clearModule
import com.quare.bibleplanner.core.datastore.di.dataStoreProviderModule
import com.quare.bibleplanner.core.date.di.dateModule
import com.quare.bibleplanner.core.devices.di.devicesModule
import com.quare.bibleplanner.core.di.modelModule
import com.quare.bibleplanner.core.loginnudge.di.loginNudgeModule
import com.quare.bibleplanner.core.network.data.di.networkModule
import com.quare.bibleplanner.core.plan.di.planModule
import com.quare.bibleplanner.core.provider.analytics.di.analyticsModule
import com.quare.bibleplanner.core.provider.billing.di.billingProviderModule
import com.quare.bibleplanner.core.provider.connectivity.di.connectivityModule
import com.quare.bibleplanner.core.provider.crashlytics.di.crashlyticsModule
import com.quare.bibleplanner.core.provider.platform.di.platformModule
import com.quare.bibleplanner.core.provider.room.di.roomModule
import com.quare.bibleplanner.core.provider.supabase.supabaseModule
import com.quare.bibleplanner.core.remoteconfig.di.remoteConfigModule
import com.quare.bibleplanner.core.review.di.reviewModule
import com.quare.bibleplanner.core.sync.di.syncModule
import com.quare.bibleplanner.core.user.di.userModule
import com.quare.bibleplanner.core.utils.di.utilsModule
import com.quare.bibleplanner.core.utils.jsonreader.di.jsonReaderModule
import com.quare.bibleplanner.feature.accountdetails.di.accountDetailsModule
import com.quare.bibleplanner.feature.addnotesfreewarning.di.addNotesFreeWarningModule
import com.quare.bibleplanner.feature.applanguage.di.appLanguageModule
import com.quare.bibleplanner.feature.bibleversion.di.bibleVersionModule
import com.quare.bibleplanner.feature.bookdetails.di.bookDetailsModule
import com.quare.bibleplanner.feature.books.di.featureBooksModule
import com.quare.bibleplanner.feature.congrats.di.congratsModule
import com.quare.bibleplanner.feature.contactsupport.di.contactSupportModule
import com.quare.bibleplanner.feature.day.di.dayModule
import com.quare.bibleplanner.feature.daystudy.di.dayStudyModule
import com.quare.bibleplanner.feature.deletenotes.di.deleteNotesModule
import com.quare.bibleplanner.feature.deleteprogress.di.deleteProgressModule
import com.quare.bibleplanner.feature.deleteversion.di.deleteVersionModule
import com.quare.bibleplanner.feature.donation.di.donationModule
import com.quare.bibleplanner.feature.donation.pixqr.di.pixQrModule
import com.quare.bibleplanner.feature.editplanstartdate.di.editPlanStartDateModule
import com.quare.bibleplanner.feature.inappupdate.di.inAppUpdateModule
import com.quare.bibleplanner.feature.login.di.loginModule
import com.quare.bibleplanner.feature.loginsyncnudge.di.loginSyncNudgeModule
import com.quare.bibleplanner.feature.loginwarning.di.loginWarningModule
import com.quare.bibleplanner.feature.logout.di.logoutModule
import com.quare.bibleplanner.feature.main.di.mainModule
import com.quare.bibleplanner.feature.materialyou.di.materialYouModule
import com.quare.bibleplanner.feature.more.di.moreModule
import com.quare.bibleplanner.feature.notificationpermission.di.notificationPermissionModule
import com.quare.bibleplanner.feature.paywall.di.paywallModule
import com.quare.bibleplanner.feature.readingplan.di.readingPlanModule
import com.quare.bibleplanner.feature.releasenotes.di.releaseNotesModule
import com.quare.bibleplanner.feature.subscriptiondetails.di.subscriptionDetailsModule
import com.quare.bibleplanner.feature.themeselection.di.themeSelectionDomainModule

object CommonKoinUtils {
    val modules = listOf(
        booksModule,
        planModule,
        clearModule,
        syncModule,
        platformModule,
        connectivityModule,
        dataStoreProviderModule,
        themeSelectionDomainModule,
        materialYouModule,
        readingPlanModule,
        dayModule,
        dayStudyModule,
        deleteProgressModule,
        deleteVersionModule,
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
        analyticsModule,
        crashlyticsModule,
        congratsModule,
        mainModule,
        moreModule,
        featureBooksModule,
        subscriptionDetailsModule,
        accountDetailsModule,
        devicesModule,
        contactSupportModule,
        releaseNotesModule,
        donationModule,
        networkModule,
        pixQrModule,
        bookDetailsModule,
        loginModule,
        loginWarningModule,
        loginSyncNudgeModule,
        loginNudgeModule,
        reviewModule,
        logoutModule,
        supabaseModule,
        userModule,
        bibleVersionModule,
        appLanguageModule,
        modelModule,
        com.quare.bibleplanner.feature.read.di.featureReadModule,
        notificationPermissionModule,
        inAppUpdateModule,
    )
}
