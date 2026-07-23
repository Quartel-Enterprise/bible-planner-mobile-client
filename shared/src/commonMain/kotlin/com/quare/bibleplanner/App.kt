package com.quare.bibleplanner

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.quare.bibleplanner.core.navigation.RootAppNavDisplay
import com.quare.bibleplanner.feature.applanguage.presentation.ApplyAppLocaleEffect
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme
import com.quare.bibleplanner.ui.theme.model.Theme
import com.quare.bibleplanner.ui.utils.LocalMainBottomBarState
import com.quare.bibleplanner.ui.utils.LocalNavigationBarInsets
import com.quare.bibleplanner.ui.utils.LocalWindowBlurController
import com.quare.bibleplanner.ui.utils.MainBottomBarState
import com.quare.bibleplanner.ui.utils.WindowBlurController
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    getSpecificColors: @Composable ((isAppInDarkTheme: Boolean) -> ColorScheme?)? = null,
    onThemeResolved: (isAppInDarkTheme: Boolean) -> Unit = {},
) {
    val viewModel: AppViewModel = koinViewModel()
    val theme by viewModel.themeState.collectAsState()
    val contrast by viewModel.contrastState.collectAsState()
    val windowBlurController = remember { WindowBlurController() }
    val mainBottomBarState = remember { MainBottomBarState() }
    LifecycleEventEffect(
        event = Lifecycle.Event.ON_START,
        onEvent = viewModel::onAppForegrounded,
    )
    ApplyAppLocaleEffect()
    ProvideCompositionLocals(
        theme = theme,
        windowBlurController = windowBlurController,
        mainBottomBarState = mainBottomBarState,
    ) {
        AppTheme(
            getSpecificColors = getSpecificColors,
            onThemeResolved = onThemeResolved,
            contrastType = contrast,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                val blurRadius = windowBlurController.radius
                RootAppNavDisplay(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
                        .then(if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier),
                )
            }
        }
    }
}

@Composable
private fun ProvideCompositionLocals(
    theme: Theme,
    windowBlurController: WindowBlurController,
    mainBottomBarState: MainBottomBarState,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalTheme provides theme,
        LocalNavigationBarInsets provides WindowInsets.navigationBars,
        LocalWindowBlurController provides windowBlurController,
        LocalMainBottomBarState provides mainBottomBarState,
        content = content,
    )
}
