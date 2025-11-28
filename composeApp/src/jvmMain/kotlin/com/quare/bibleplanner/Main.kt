package com.quare.bibleplanner

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import bibleplanner.composeapp.generated.resources.Res
import bibleplanner.composeapp.generated.resources.app_title
import com.quare.bibleplanner.core.books.data.datasource.JvmResourceReader
import com.quare.bibleplanner.core.books.data.datasource.ResourceReader
import com.quare.bibleplanner.core.provider.koin.initializeKoin
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import org.jetbrains.compose.resources.stringResource
import org.koin.dsl.module

fun main() = application {
    initializeKoin(
        platformModules = listOf(
            module {
                single { getDatabaseBuilder() }
                single<ResourceReader> { JvmResourceReader() }
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
