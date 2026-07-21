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
import com.quare.bibleplanner.feature.bookdetails.presentation.bookDetails
import com.quare.bibleplanner.feature.congrats.presentation.congrats
import com.quare.bibleplanner.feature.contactsupport.presentation.contactSupport
import com.quare.bibleplanner.feature.day.presentation.day
import com.quare.bibleplanner.feature.daystudy.presentation.dayStudy
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
import com.quare.bibleplanner.feature.read.presentation.read
import com.quare.bibleplanner.feature.releasenotes.presentation.releaseNotes
import com.quare.bibleplanner.feature.subscriptiondetails.presentation.subscriptionDetails
import com.quare.bibleplanner.feature.themeselection.presentation.themeSettings

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.toEntryProvider(
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (NavKey) -> Unit,
    onNavigate: (NavKey) -> Unit,
): (NavKey) -> NavEntry<NavKey> = entryProvider {
    val sharedTransitionScope = this@toEntryProvider
    loginRoot(onNavigateBack)
    loginWarning(
        onNavigateBack = onNavigateBack,
        onNavigateReplacingTop = onNavigateReplacingTop,
    )
    loginSyncNudge(
        onNavigateBack = onNavigateBack,
        onNavigateReplacingTop = onNavigateReplacingTop,
    )
    logout(
        onNavigateBack = onNavigateBack,
    )
    mainScreen(
        onNavigate = onNavigate,
        sharedTransitionScope = sharedTransitionScope,
    )
    day(
        onNavigate = onNavigate,
        onNavigateBack = onNavigateBack,
        sharedTransitionScope = sharedTransitionScope,
    )
    dayStudy(
        onNavigate = onNavigate,
        onNavigateBack = onNavigateBack,
    )
    themeSettings(
        onNavigate = onNavigate,
        onNavigateBack = onNavigateBack,
    )
    materialYou(onNavigateBack)
    deleteProgress(onNavigateBack)
    deleteNotes(onNavigateBack)
    addNotesFreeWarning(
        onNavigateBack = onNavigateBack,
        onNavigateReplacingTop = onNavigateReplacingTop,
    )
    editPlanStartDate(onNavigateBack)
    releaseNotes(
        onNavigateBack = onNavigateBack,
        sharedTransitionScope = sharedTransitionScope,
    )
    paywall(
        onNavigate = onNavigate,
        onNavigateBack = onNavigateBack,
        onNavigateReplacingTop = onNavigateReplacingTop,
        sharedTransitionScope = sharedTransitionScope,
    )
    congrats(onNavigateBack)
    donation(
        onNavigate = onNavigate,
        onNavigateBack = onNavigateBack,
    )
    pixQr(onNavigateBack)
    bookDetails(
        onNavigate = onNavigate,
        onNavigateBack = onNavigateBack,
        sharedTransitionScope = sharedTransitionScope,
    )
    appLanguage(
        onNavigate = onNavigate,
        onNavigateBack = onNavigateBack,
    )
    bibleVersionSelectionRoot(
        onNavigate = onNavigate,
        onNavigateBack = onNavigateBack,
    )
    deleteVersion(onNavigateBack)
    subscriptionDetails(onNavigateBack)
    accountDetails(
        onNavigateBack = onNavigateBack,
        onNavigateReplacingTop = onNavigateReplacingTop,
        onNavigate = onNavigate,
    )
    renameDevice(onNavigateBack)
    editProfile(
        onNavigateReplacingTop = onNavigateReplacingTop,
        onNavigateBack = onNavigateBack,
    )
    editName(onNavigateBack)
    editPhotoSource(
        onNavigateReplacingTop = onNavigateReplacingTop,
        onNavigateBack = onNavigateBack,
    )
    expandedPhoto(
        onNavigate = onNavigate,
        onNavigateBack = onNavigateBack,
    )
    cropPhoto(onNavigateBack)
    contactSupport(onNavigateBack)
    read(
        onNavigate = onNavigate,
        onNavigateBack = onNavigateBack,
        onNavigateReplacingTop = onNavigateReplacingTop,
        sharedTransitionScope = sharedTransitionScope,
    )
    notificationPermission(onNavigateBack)
    inAppUpdate(onNavigateBack)
    updateDownloaded(onNavigateBack)
}
