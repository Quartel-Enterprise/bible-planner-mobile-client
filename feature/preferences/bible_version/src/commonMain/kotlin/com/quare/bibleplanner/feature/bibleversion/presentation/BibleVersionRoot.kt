package com.quare.bibleplanner.feature.bibleversion.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.preferences.bible_version.generated.resources.Res
import bibleplanner.feature.preferences.bible_version.generated.resources.bible_versions
import bibleplanner.feature.preferences.bible_version.generated.resources.manage_bible_versions_description
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.bibleversion.presentation.component.BibleVersionsContent
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.bibleVersionSelectionRoot() {
    entry<BibleVersionSelectorRoute>(metadata = getSheetPane()) {
        val viewModel: BibleVersionViewModel = koinViewModel()
        val onEvent = viewModel::onEvent
        val uiState by viewModel.uiState.collectAsState()
        ResponsiveDialogSheet(
            onCloseClick = { onEvent(BibleVersionUiEvent.OnDismiss) },
            title = stringResource(Res.string.bible_versions),
            subtitle = stringResource(Res.string.manage_bible_versions_description),
            isHeightCapped = true,
        ) {
            BibleVersionsContent(
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .padding(horizontal = 16.dp),
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}
