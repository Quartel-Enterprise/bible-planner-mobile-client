package com.quare.bibleplanner.nativeui

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.feature.addnotesfreewarning.toNavEntry
import com.quare.bibleplanner.android.feature.congrats.toNavEntry
import com.quare.bibleplanner.android.feature.deletenotes.toNavEntry
import com.quare.bibleplanner.android.feature.deleteprogress.toNavEntry
import com.quare.bibleplanner.android.feature.deleteversion.toNavEntry
import com.quare.bibleplanner.android.feature.editplanstartdate.toNavEntry
import com.quare.bibleplanner.android.feature.loginsyncnudge.toNavEntry
import com.quare.bibleplanner.android.feature.loginwarning.toNavEntry
import com.quare.bibleplanner.android.feature.materialyou.toNavEntry
import com.quare.bibleplanner.android.feature.notificationpermission.toNavEntry
import com.quare.bibleplanner.android.feature.paywallteaser.toNavEntry
import com.quare.bibleplanner.android.feature.pixqr.toNavEntry
import com.quare.bibleplanner.android.feature.subscriptiondetails.toNavEntry
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.CongratsNavRoute
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
import com.quare.bibleplanner.core.model.route.LoginSyncNudgeNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.MaterialYouBottomSheetNavRoute
import com.quare.bibleplanner.core.model.route.NotificationPermissionNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PixQrNavRoute
import com.quare.bibleplanner.core.model.route.SubscriptionDetailsNavRoute
import com.quare.bibleplanner.core.navigation.NativeEntryProvider

internal class AndroidNativeEntryProvider : NativeEntryProvider {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = when (key) {
        is DeleteNotesRoute -> key.toNavEntry()
        is LoginWarningNavRoute -> key.toNavEntry()
        is AddNotesFreeWarningNavRoute -> key.toNavEntry()
        is LoginSyncNudgeNavRoute -> key.toNavEntry()
        is DeleteVersionNavRoute -> key.toNavEntry()
        is DeleteAllProgressNavRoute -> key.toNavEntry()
        is CongratsNavRoute -> key.toNavEntry()
        is NotificationPermissionNavRoute -> key.toNavEntry()
        is EditPlanStartDateNavRoute -> key.toNavEntry()
        is MaterialYouBottomSheetNavRoute -> key.toNavEntry()
        is PixQrNavRoute -> key.toNavEntry()
        is PaywallTeaserNavRoute -> key.toNavEntry()
        is SubscriptionDetailsNavRoute -> key.toNavEntry()
        else -> null
    }
}
