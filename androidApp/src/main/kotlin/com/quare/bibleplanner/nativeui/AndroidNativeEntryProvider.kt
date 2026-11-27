package com.quare.bibleplanner.nativeui

import com.quare.bibleplanner.android.feature.deletenotes.toNavEntry
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.core.navigation.NativeEntryProvider
import org.koin.dsl.module

internal val androidNativeUiModule = module {
    single<NativeEntryProvider> {
        NativeEntryProvider { key ->
            if (key is DeleteNotesRoute) key.toNavEntry() else null
        }
    }
}
