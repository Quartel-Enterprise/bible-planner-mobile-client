package com.quare.bibleplanner.nativeui

import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute
import com.quare.bibleplanner.core.navigation.NativeEntryProvider
import com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen.ReleaseNotesNativeRoot

internal class IosNativeEntryProvider : NativeEntryProvider {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = if (key is ReleaseNotesNavRoute) {
        NavEntry(key) {
            val uriHandler = LocalUriHandler.current
            ReleaseNotesNativeRoot(openUrl = { url -> uriHandler.openUri(url) })
        }
    } else {
        null
    }
}
