package com.quare.bibleplanner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.utils.orFalse
import com.quare.bibleplanner.notification.AndroidBibleVersionDownloadNotifier
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNotificationIntent(intent)

        setContent {
            val isDynamicColorsOn by viewModel.isDynamicColorsEnabledFlow.collectAsState(true)
            App(
                getSpecificColors = { isAppInDarkTheme ->
                    enableEdgeToEdge(statusBarStyle = getStatusBarStyle(isAppInDarkTheme))
                    getAndroidSpecificColorScheme(
                        isDynamicColorsOn = isDynamicColorsOn,
                        isAppInDarkTheme = isAppInDarkTheme,
                    )
                },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.shouldOpenBibleVersionManager().orFalse()) {
            viewModel.navigationEventBus.send(BibleVersionSelectorRoute)
        }
    }

    private fun Intent.shouldOpenBibleVersionManager(): Boolean =
        getBooleanExtra(AndroidBibleVersionDownloadNotifier.EXTRA_NAVIGATE_TO_BIBLE_VERSIONS, false)

    @Composable
    private fun getStatusBarStyle(isAppInDarkTheme: Boolean): SystemBarStyle = SystemBarStyle.run {
        val color = Color.Transparent.toArgb()
        if (isAppInDarkTheme) {
            dark(color)
        } else {
            light(color, color)
        }
    }
}
