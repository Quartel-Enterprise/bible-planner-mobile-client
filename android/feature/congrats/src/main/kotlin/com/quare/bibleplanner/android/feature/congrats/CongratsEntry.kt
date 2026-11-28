package com.quare.bibleplanner.android.feature.congrats

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.CongratsNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.congrats.presentation.viewmodel.CongratsViewModel
import org.koin.compose.viewmodel.koinViewModel

fun CongratsNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<CongratsViewModel>()
    ExpressiveTheme {
        CongratsContent(onEvent = viewModel::onEvent)
    }
}
