package com.quare.bibleplanner

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.navigation.RootAppNavDisplay
import com.quare.bibleplanner.feature.applanguage.presentation.ApplyAppLocaleEffect
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme
import com.quare.bibleplanner.ui.theme.model.Theme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(getSpecificColors: @Composable ((isAppInDarkTheme: Boolean) -> ColorScheme?)? = null) {
    val viewModel: AppViewModel = koinViewModel()
    val theme by viewModel.themeState.collectAsState()
    val contrast by viewModel.contrastState.collectAsState()
    ApplyAppLocaleEffect()
    ProvideCompositionLocals(theme) {
        AppTheme(
            getSpecificColors = getSpecificColors,
            contrastType = contrast,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                RootAppNavDisplay()
            }
        }
    }
}

@Composable
private fun ProvideCompositionLocals(
    theme: Theme,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalTheme provides theme,
        content = content,
    )
}
