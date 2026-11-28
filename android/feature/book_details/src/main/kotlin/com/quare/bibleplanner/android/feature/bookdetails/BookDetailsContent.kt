package com.quare.bibleplanner.android.feature.bookdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.presentation.model.titleRes
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiEvent
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiState
import com.quare.bibleplanner.feature.bookdetails.presentation.utils.toSynopsisResource
import com.quare.bibleplanner.ui.utils.SharedTransitionModifierFactory
import androidx.compose.ui.res.stringResource as androidStringResource
import org.jetbrains.compose.resources.stringResource as sharedStringResource

// Why: the same breakpoint and width cap as the shared screen's ResponsiveColumn.
private val twoColumnsMinWidth = 600.dp
private val contentMaxWidth = 800.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookDetailsContent(
    state: BookDetailsUiState,
    transitionScopes: BookDetailsTransitionScopes?,
    onEvent: (BookDetailsUiEvent) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BookDetailsTopBar(
                state = state,
                transitionScopes = transitionScopes,
                scrollBehavior = scrollBehavior,
                onEvent = onEvent,
            )
        },
    ) { innerPadding ->
        when (state) {
            BookDetailsUiState.Loading -> Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is BookDetailsUiState.Success -> BookDetailsLoadedContent(
                state = state,
                transitionScopes = transitionScopes,
                onEvent = onEvent,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDetailsTopBar(
    state: BookDetailsUiState,
    transitionScopes: BookDetailsTransitionScopes?,
    scrollBehavior: TopAppBarScrollBehavior,
    onEvent: (BookDetailsUiEvent) -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            if (state is BookDetailsUiState.Success) {
                val bookName = sharedStringResource(state.id.toBookNameResource())
                Text(
                    text = bookName,
                    modifier = if (transitionScopes == null) {
                        Modifier
                    } else {
                        SharedTransitionModifierFactory.getTopBarBookNameSharedTransitionModifier(
                            sharedTransitionScope = transitionScopes.sharedTransitionScope,
                            animatedVisibilityScope = transitionScopes.animatedVisibilityScope,
                            bookName = bookName,
                        )
                    },
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { onEvent(BookDetailsUiEvent.OnBackClick) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = androidStringResource(R.string.book_details_back),
                )
            }
        },
        actions = {
            if (state is BookDetailsUiState.Success) {
                IconButton(onClick = { onEvent(BookDetailsUiEvent.OnToggleFavorite) }) {
                    Icon(
                        imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = androidStringResource(R.string.book_details_favorite),
                        tint = if (state.isFavorite) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.sharedElementOf(
                            transitionScopes = transitionScopes,
                            key = "favorite-${state.id.name}",
                        ),
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun BookDetailsLoadedContent(
    state: BookDetailsUiState.Success,
    transitionScopes: BookDetailsTransitionScopes?,
    onEvent: (BookDetailsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bookName = sharedStringResource(state.id.toBookNameResource())
    val synopsis = sharedStringResource(state.id.toSynopsisResource())
    val bookGroupName = sharedStringResource(state.bookGroup.titleRes)
    val progressCard: @Composable () -> Unit = {
        BookDetailsProgressCard(
            progress = state.progress,
            readChaptersCount = state.readChaptersCount,
            totalChaptersCount = state.totalChaptersCount,
            bookIdName = state.id.name,
            transitionScopes = transitionScopes,
        )
    }
    val synopsisSection: @Composable () -> Unit = {
        BookDetailsSynopsisSection(
            synopsis = synopsis,
            bookGroupName = bookGroupName,
            bookGroupSharedKey = "book-group-${state.bookGroup}",
            isExpanded = state.isSynopsisExpanded,
            transitionScopes = transitionScopes,
            onToggleExpanded = { onEvent(BookDetailsUiEvent.OnToggleSynopsisExpanded) },
        )
    }
    val chaptersGrid: @Composable () -> Unit = {
        BookDetailsChaptersGrid(
            bookName = bookName,
            chapters = state.chapters,
            areAllChaptersRead = state.areAllChaptersRead,
            transitionScopes = transitionScopes,
            onChapterClick = { chapterNumber -> onEvent(BookDetailsUiEvent.OnChapterClick(chapterNumber)) },
            onToggleAllClick = { onEvent(BookDetailsUiEvent.OnToggleAllChapters) },
        )
    }
    BoxWithConstraints(modifier = modifier.padding(horizontal = 16.dp)) {
        val isTwoColumns = maxWidth > twoColumnsMinWidth
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isTwoColumns) {
                centeredItem(key = "two_columns") {
                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            progressCard()
                            synopsisSection()
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            chaptersGrid()
                        }
                    }
                }
            } else {
                centeredItem(
                    key = "progress_card",
                    content = progressCard,
                )
                centeredItem(
                    key = "synopsis",
                    content = synopsisSection,
                )
                centeredItem(
                    key = "chapters",
                    content = chaptersGrid,
                )
            }
        }
    }
}

private fun LazyListScope.centeredItem(
    key: String,
    content: @Composable () -> Unit,
) {
    item(key = key) {
        Column(
            modifier = Modifier
                .widthIn(max = contentMaxWidth)
                .fillMaxWidth(),
        ) {
            content()
        }
    }
}
