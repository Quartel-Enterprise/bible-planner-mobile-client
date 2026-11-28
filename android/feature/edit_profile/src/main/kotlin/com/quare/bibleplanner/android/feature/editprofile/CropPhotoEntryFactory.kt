package com.quare.bibleplanner.android.feature.editprofile

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.CropPhotoNavRoute

internal class CropPhotoEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? CropPhotoNavRoute)?.toNavEntry()
}
