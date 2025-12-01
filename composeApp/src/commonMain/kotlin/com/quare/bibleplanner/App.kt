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
import com.quare.bibleplanner.core.navigation.AppNavHost
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme
import com.quare.bibleplanner.ui.theme.model.Theme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(getSpecificColors: @Composable ((isAppInDarkTheme: Boolean) -> ColorScheme?)? = null) {
    val viewModel: AppViewModel = koinViewModel()
    val theme by viewModel.themeState.collectAsState()
    ProvideCompositionLocals(theme) {
        AppTheme(getSpecificColors) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                AppNavHost()
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
