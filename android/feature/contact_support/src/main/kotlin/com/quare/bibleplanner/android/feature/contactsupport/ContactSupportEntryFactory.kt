package com.quare.bibleplanner.android.feature.contactsupport

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.ContactSupportNavRoute

internal class ContactSupportEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? ContactSupportNavRoute)?.toNavEntry()
}
