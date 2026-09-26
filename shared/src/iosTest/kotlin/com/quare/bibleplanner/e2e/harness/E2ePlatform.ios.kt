package com.quare.bibleplanner.e2e.harness

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.quare.bibleplanner.AppRoot
import com.quare.bibleplanner.core.provider.language.di.iosLanguageProviderModule
import com.quare.bibleplanner.core.provider.language.di.languageProviderModule
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.feature.applanguage.di.iosAppLanguageModule
import com.quare.bibleplanner.feature.login.di.iosLoginModule
import com.quare.bibleplanner.ui.testing.setUiTestContent
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

// The flows compile for iOS but don't run there: the build filters them out of the simulator tests,
// because the main tabs are Calf's native UITabBar, which a Compose UI test can't reach.
internal actual val e2ePlatform: E2ePlatform = IosE2ePlatform

private object IosE2ePlatform : E2ePlatform {
    override val modules: List<Module> = listOf(
        iosAppLanguageModule,
        iosLanguageProviderModule,
        iosLoginModule,
        languageProviderModule,
        module {
            single { Room.inMemoryDatabaseBuilder<AppDatabase>().setDriver(BundledSQLiteDriver()) }
        },
    )

    override fun configure(koinApplication: KoinApplication) = Unit

    @OptIn(ExperimentalForeignApi::class)
    override fun createDirectory(): String {
        val path = NSTemporaryDirectory() + "$E2E_DIRECTORY_PREFIX-${NSUUID().UUIDString}"
        NSFileManager.defaultManager.createDirectoryAtPath(
            path = path,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
        return path
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun deleteDirectory(path: String) {
        NSFileManager.defaultManager.removeItemAtPath(
            path = path,
            error = null,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    override fun ComposeUiTest.showApp(
        window: E2eWindow,
        viewModelStoreOwner: ViewModelStoreOwner,
    ): AutoCloseable {
        setUiTestContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
        return AutoCloseable {}
    }

    private const val E2E_DIRECTORY_PREFIX = "bible-planner-e2e"
}
