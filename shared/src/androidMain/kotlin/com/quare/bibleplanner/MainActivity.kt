package com.quare.bibleplanner

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.supabase.SupabaseDeeplinkHandler
import com.quare.bibleplanner.core.utils.orFalse
import com.quare.bibleplanner.notification.AndroidBibleVersionDownloadNotifier
import com.quare.bibleplanner.notification.NotificationAnalyticsType
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModel()
    private val supabaseDeeplinkHandler: SupabaseDeeplinkHandler by inject()
    private val trackEvent: TrackEvent by inject()

    override fun attachBaseContext(newBase: Context) {
        val localeTag = newBase
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_LANGUAGE, null)
        if (localeTag != null) {
            val locale = Locale.forLanguageTag(localeTag)
            Locale.setDefault(locale)
            val config = Configuration(newBase.resources.configuration)
            config.setLocale(locale)
            super.attachBaseContext(newBase.createConfigurationContext(config))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supabaseDeeplinkHandler.handle(intent)
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
        supabaseDeeplinkHandler.handle(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.shouldOpenBibleVersionManager().orFalse()) {
            trackEvent(
                name = AnalyticsEventNames.NOTIFICATION_OPENED,
                params = mapOf(AnalyticsParams.TYPE to NotificationAnalyticsType.VERSION_DOWNLOAD_COMPLETE),
            )
            viewModel.navigationEventBus.send(BibleVersionSelectorRoute)
        }
    }

    private fun Intent.shouldOpenBibleVersionManager(): Boolean =
        getBooleanExtra(AndroidBibleVersionDownloadNotifier.EXTRA_NAVIGATE_TO_BIBLE_VERSIONS, false)

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_APP_LANGUAGE = "app_language"
    }

    private fun getStatusBarStyle(isAppInDarkTheme: Boolean): SystemBarStyle = SystemBarStyle.run {
        val color = Color.Transparent.toArgb()
        if (isAppInDarkTheme) {
            dark(color)
        } else {
            light(color, color)
        }
    }
}
