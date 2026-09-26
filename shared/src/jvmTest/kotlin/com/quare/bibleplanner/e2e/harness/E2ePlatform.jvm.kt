package com.quare.bibleplanner.e2e.harness

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.quare.bibleplanner.AppRoot
import com.quare.bibleplanner.core.provider.language.di.jvmLanguageProviderModule
import com.quare.bibleplanner.core.provider.language.di.languageProviderModule
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.feature.applanguage.di.jvmAppLanguageModule
import com.quare.bibleplanner.feature.login.di.jvmLoginModule
import com.quare.bibleplanner.ui.testing.setUiTestContent
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File
import java.nio.file.Files
import java.util.Locale

internal actual val e2ePlatform: E2ePlatform = JvmE2ePlatform

private object JvmE2ePlatform : E2ePlatform {
    override val modules: List<Module> = listOf(
        jvmAppLanguageModule,
        jvmLanguageProviderModule,
        jvmLoginModule,
        languageProviderModule,
        module {
            single { Room.inMemoryDatabaseBuilder<AppDatabase>().setDriver(BundledSQLiteDriver()) }
        },
    )

    override fun configure(koinApplication: KoinApplication) = Unit

    override fun createDirectory(): String = Files.createTempDirectory(E2E_DIRECTORY_PREFIX).toString()

    override fun deleteDirectory(path: String) {
        File(path).deleteRecursively()
    }

    // The desktop app applies its language with Locale.setDefault, which outlives the test, so every
    // test starts in English and puts the machine's locale back when it ends.
    @OptIn(ExperimentalTestApi::class)
    override fun ComposeUiTest.showApp(
        window: E2eWindow,
        viewModelStoreOwner: ViewModelStoreOwner,
    ): AutoCloseable {
        val machineLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)
        setUiTestContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                Box(modifier = Modifier.requiredSize(window.size)) {
                    AppRoot()
                }
            }
        }
        return AutoCloseable { Locale.setDefault(machineLocale) }
    }

    private const val E2E_DIRECTORY_PREFIX = "bible-planner-e2e"
}

// Both fit in the 1024x768 window of the desktop test host, which is too small to show either whole.
private val E2eWindow.size: DpSize
    get() = when (this) {
        E2eWindow.PORTRAIT -> DpSize(width = 400.dp, height = 720.dp)
        E2eWindow.WIDE -> DpSize(width = 1000.dp, height = 720.dp)
    }
