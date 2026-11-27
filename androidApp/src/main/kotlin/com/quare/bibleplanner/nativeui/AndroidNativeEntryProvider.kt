package com.quare.bibleplanner.nativeui

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.feature.addnotesfreewarning.toNavEntry
import com.quare.bibleplanner.android.feature.deletenotes.toNavEntry
import com.quare.bibleplanner.android.feature.loginsyncnudge.toNavEntry
import com.quare.bibleplanner.android.feature.loginwarning.toNavEntry
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.core.model.route.LoginSyncNudgeNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.navigation.NativeEntryProvider

internal class AndroidNativeEntryProvider : NativeEntryProvider {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = when (key) {
        is DeleteNotesRoute -> key.toNavEntry()
        is LoginWarningNavRoute -> key.toNavEntry()
        is AddNotesFreeWarningNavRoute -> key.toNavEntry()
        is LoginSyncNudgeNavRoute -> key.toNavEntry()
        else -> null
    }
}
