package com.quare.bibleplanner.e2e.harness

import android.Manifest
import android.app.UiAutomation
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.Surface
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.core.content.edit
import androidx.lifecycle.ViewModelStoreOwner
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.quare.bibleplanner.MainActivity
import com.quare.bibleplanner.core.provider.language.di.androidLanguageProviderModule
import com.quare.bibleplanner.core.provider.language.di.languageProviderModule
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.supabase.di.androidSupabaseModule
import com.quare.bibleplanner.di.androidModule
import com.quare.bibleplanner.feature.applanguage.di.androidAppLanguageModule
import com.quare.bibleplanner.feature.login.di.androidLoginModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File
import java.util.UUID

internal actual val e2ePlatform: E2ePlatform = AndroidE2ePlatform

private object AndroidE2ePlatform : E2ePlatform {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    private val uiAutomation: UiAutomation
        get() = InstrumentationRegistry.getInstrumentation().uiAutomation

    override val modules: List<Module> = listOf(
        androidModule,
        androidAppLanguageModule,
        androidLanguageProviderModule,
        androidLoginModule,
        androidSupabaseModule,
        languageProviderModule,
        module {
            single { Room.inMemoryDatabaseBuilder<AppDatabase>(context).setDriver(AndroidSQLiteDriver()) }
        },
    )

    override fun configure(koinApplication: KoinApplication) {
        koinApplication.androidContext(context)
    }

    override fun createDirectory(): String = File(context.cacheDir, "$E2E_DIRECTORY_PREFIX-${UUID.randomUUID()}")
        .apply { mkdirs() }
        .absolutePath

    override fun deleteDirectory(path: String) {
        File(path).deleteRecursively()
    }

    // The app's own activity, which is the ViewModelStoreOwner here and clears its ViewModels when it
    // closes. It reads the language from its preferences before it draws, so the test writes English
    // there first and removes it at the end, since those preferences outlive the test. The wide
    // window is the device in landscape. The device turns before the activity starts: turning the
    // activity itself recreates it, and a recreation that lands in the middle of a flow leaves the
    // test with no Compose hierarchy to query.
    @OptIn(ExperimentalTestApi::class)
    override fun ComposeUiTest.showApp(
        window: E2eWindow,
        viewModelStoreOwner: ViewModelStoreOwner,
    ): AutoCloseable {
        val appPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        appPreferences.edit(commit = true) { putString(APP_LANGUAGE_KEY, ENGLISH_LANGUAGE_TAG) }
        allowNotifications()
        turnTheDevice(window)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        return AutoCloseable {
            scenario.close()
            uiAutomation.setRotation(UiAutomation.ROTATION_UNFREEZE)
            appPreferences.edit(commit = true) { clear() }
        }
    }

    // Downloading a Bible version asks for this permission, and the system dialog that asks for it
    // pauses the app, which leaves the test with no Compose hierarchy until it goes away. The flows
    // are for a user who has already allowed notifications.
    private fun allowNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    private fun ComposeUiTest.turnTheDevice(window: E2eWindow) {
        uiAutomation.setRotation(window.rotation)
        waitUntil(timeoutMillis = ROTATION_TIMEOUT_MILLIS) {
            context.getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY).rotation ==
                window.displayRotation
        }
    }

    private const val E2E_DIRECTORY_PREFIX = "e2e"
    private const val ROTATION_TIMEOUT_MILLIS = 10_000L
    private const val APP_PREFERENCES = "app_prefs"
    private const val APP_LANGUAGE_KEY = "app_language"
    private const val ENGLISH_LANGUAGE_TAG = "en"
}

private val E2eWindow.rotation: Int
    get() = when (this) {
        E2eWindow.PORTRAIT -> UiAutomation.ROTATION_FREEZE_0
        E2eWindow.WIDE -> UiAutomation.ROTATION_FREEZE_90
    }

private val E2eWindow.displayRotation: Int
    get() = when (this) {
        E2eWindow.PORTRAIT -> Surface.ROTATION_0
        E2eWindow.WIDE -> Surface.ROTATION_90
    }
