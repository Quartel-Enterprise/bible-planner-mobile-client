package com.quare.bibleplanner.android.feature.studyunlock

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute

internal class StudyUnlockEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? StudyUnlockNavRoute)?.toNavEntry()
}
