package com.quare.bibleplanner.android.feature.releasenotes

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import com.quare.bibleplanner.feature.releasenotes.presentation.mapper.toVisibleNotes
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiEvent
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseVersionType
import com.quare.bibleplanner.feature.releasenotes.presentation.utils.tagBackgroundColor
import com.quare.bibleplanner.feature.releasenotes.presentation.utils.tagContentColor
import com.quare.bibleplanner.ui.utils.format

private val contentMaxWidth = 800.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ReleaseNotesContent(
    uiState: ReleaseNotesUiState,
    onEvent: (ReleaseNotesUiEvent) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.release_notes_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ReleaseNotesUiEvent.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.release_notes_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(ReleaseNotesUiEvent.OnGithubAllReleasesClicked) }) {
                        Icon(
                            painter = painterResource(R.drawable.release_notes_ic_github),
                            contentDescription = stringResource(R.string.release_notes_view_all_github),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        when (uiState) {
            ReleaseNotesUiState.Loading -> CenteredBox(paddingValues) { LoadingIndicator() }

            ReleaseNotesUiState.Error -> CenteredBox(paddingValues) {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = stringResource(R.string.release_notes_error_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }

            is ReleaseNotesUiState.Success -> ReleaseNotesList(
                state = uiState,
                paddingValues = paddingValues,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun ReleaseNotesList(
    state: ReleaseNotesUiState.Success,
    paddingValues: PaddingValues,
    onEvent: (ReleaseNotesUiEvent) -> Unit,
) {
    val selectedTabIndex = state.availableTabs.indexOf(state.currentTab).coerceAtLeast(0)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SecondaryTabRow(
            modifier = Modifier.widthIn(max = contentMaxWidth),
            selectedTabIndex = selectedTabIndex,
        ) {
            state.availableTabs.forEach { tab ->
                Tab(
                    selected = state.currentTab == tab,
                    onClick = { onEvent(ReleaseNotesUiEvent.OnTabSelected(tab)) },
                    text = { Text(text = stringResource(tab.toTitle())) },
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 16.dp + paddingValues.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(
                items = state.toVisibleNotes(),
                key = { visibleNote -> visibleNote.note.version },
            ) { visibleNote ->
                ReleaseNoteCard(
                    note = visibleNote.note,
                    type = visibleNote.type,
                    onClick = { onEvent(ReleaseNotesUiEvent.OnGithubVersionClicked(visibleNote.note.version)) },
                )
            }
        }
    }
}

@Composable
private fun ReleaseNoteCard(
    note: ReleaseNoteModel,
    type: ReleaseVersionType,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        enabled = type.isClickable,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = contentMaxWidth),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.version,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    note.dateRepresentation?.let { representation ->
                        Text(
                            text = representation.format(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                type.toTag()?.let { tag ->
                    ReleaseNoteTagLabel(
                        text = stringResource(tag),
                        type = type,
                    )
                }
            }
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                note.changes.forEach { change -> ChangeRow(change = change) }
            }
        }
    }
}

@Composable
private fun ReleaseNoteTagLabel(
    text: String,
    type: ReleaseVersionType,
) {
    Surface(
        color = type.tagBackgroundColor(),
        contentColor = type.tagContentColor(),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp,
            ),
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ChangeRow(change: String) {
    Row {
        Text(
            modifier = Modifier.padding(end = 8.dp),
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = change,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CenteredBox(
    paddingValues: PaddingValues,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@StringRes
private fun ReleaseNotesTab.toTitle(): Int = when (this) {
    ReleaseNotesTab.LATEST -> R.string.release_notes_tab_latest
    ReleaseNotesTab.UPCOMING -> R.string.release_notes_tab_upcoming
    ReleaseNotesTab.PAST_VERSIONS -> R.string.release_notes_tab_past
}

@StringRes
private fun ReleaseVersionType.toTag(): Int? = when (this) {
    ReleaseVersionType.LATEST -> R.string.release_notes_tag_latest
    ReleaseVersionType.UPCOMING -> R.string.release_notes_tag_upcoming
    ReleaseVersionType.PAST -> null
}
