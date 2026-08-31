package com.quare.bibleplanner.core.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.quare.bibleplanner.feature.accountdetails.presentation.accountDetails
import com.quare.bibleplanner.feature.accountdetails.presentation.renameDevice
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.addNotesFreeWarning
import com.quare.bibleplanner.feature.applanguage.presentation.appLanguage
import com.quare.bibleplanner.feature.bibleversion.presentation.bibleVersionSelectionRoot
import com.quare.bibleplanner.feature.bibleversion.presentation.pendingBibleUpdates
import com.quare.bibleplanner.feature.bookdetails.presentation.bookDetails
import com.quare.bibleplanner.feature.chat.presentation.chat
import com.quare.bibleplanner.feature.congrats.presentation.congrats
import com.quare.bibleplanner.feature.contactsupport.presentation.contactSupport
import com.quare.bibleplanner.feature.day.presentation.day
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.dayReadingComplete
import com.quare.bibleplanner.feature.daystudy.presentation.dayStudy
import com.quare.bibleplanner.feature.deleteaccount.presentation.deleteAccount
import com.quare.bibleplanner.feature.deletenotes.presentation.deleteNotes
import com.quare.bibleplanner.feature.deleteprogress.presentation.deleteProgress
import com.quare.bibleplanner.feature.deleteversion.presentation.deleteVersion
import com.quare.bibleplanner.feature.donation.pixqr.presentation.pixQr
import com.quare.bibleplanner.feature.donation.presentation.donation
import com.quare.bibleplanner.feature.editplanstartdate.presentation.editPlanStartDate
import com.quare.bibleplanner.feature.editprofile.presentation.cropPhoto
import com.quare.bibleplanner.feature.editprofile.presentation.editName
import com.quare.bibleplanner.feature.editprofile.presentation.editPhotoSource
import com.quare.bibleplanner.feature.editprofile.presentation.editProfile
import com.quare.bibleplanner.feature.editprofile.presentation.expandedPhoto
import com.quare.bibleplanner.feature.inappupdate.presentation.inAppUpdate
import com.quare.bibleplanner.feature.inappupdate.presentation.updateDownloaded
import com.quare.bibleplanner.feature.login.presentation.loginRoot
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.loginSyncNudge
import com.quare.bibleplanner.feature.loginwarning.presentation.loginWarning
import com.quare.bibleplanner.feature.logout.presentation.logout
import com.quare.bibleplanner.feature.main.presentation.mainScreen
import com.quare.bibleplanner.feature.materialyou.presentation.materialYou
import com.quare.bibleplanner.feature.notificationpermission.presentation.notificationPermission
import com.quare.bibleplanner.feature.paywall.presentation.paywall
import com.quare.bibleplanner.feature.paywallteaser.presentation.paywallTeaser
import com.quare.bibleplanner.feature.read.presentation.read
import com.quare.bibleplanner.feature.releasenotes.presentation.releaseNotes
import com.quare.bibleplanner.feature.subscriptiondetails.presentation.subscriptionDetails
import com.quare.bibleplanner.feature.themeselection.presentation.themeSettings
import com.quare.bibleplanner.feature.verse.addnote.presentation.verseNote
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.verseSelection
import com.quare.bibleplanner.feature.verse.share.presentation.shareVerse

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.toEntryProvider(): (NavKey) -> NavEntry<NavKey> = entryProvider {
    val sharedTransitionScope = this@toEntryProvider
    loginRoot()
    loginWarning()
    loginSyncNudge()
    logout()
    mainScreen(sharedTransitionScope)
    day(sharedTransitionScope)
    dayStudy()
    dayReadingComplete()
    chat()
    themeSettings()
    materialYou()
    deleteProgress()
    deleteAccount()
    deleteNotes()
    addNotesFreeWarning()
    editPlanStartDate()
    releaseNotes(sharedTransitionScope)
    paywall(sharedTransitionScope)
    paywallTeaser()
    congrats()
    donation()
    pixQr()
    bookDetails(sharedTransitionScope)
    appLanguage()
    bibleVersionSelectionRoot()
    deleteVersion()
    subscriptionDetails()
    accountDetails()
    renameDevice()
    editProfile()
    editName()
    editPhotoSource()
    expandedPhoto()
    cropPhoto()
    contactSupport()
    read()
    verseNote()
    verseSelection()
    shareVerse()
    notificationPermission()
    inAppUpdate()
    updateDownloaded()
    pendingBibleUpdates()
}
