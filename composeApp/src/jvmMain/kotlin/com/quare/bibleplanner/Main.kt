package com.quare.bibleplanner

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import bibleplanner.composeapp.generated.resources.Res
import bibleplanner.composeapp.generated.resources.app_title
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.di.initializeKoin
import org.jetbrains.compose.resources.stringResource
import org.koin.dsl.module

fun main() = application {
    initializeKoin(
        platformModules = listOf(
            module {
                single { getDatabaseBuilder() }
            },
        ),
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_title),
    ) {
        App()
    }
}
