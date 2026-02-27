package com.quare.bibleplanner.feature.read.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.chapter_not_downloaded_error
import bibleplanner.feature.read.generated.resources.manage_bible_versions
import bibleplanner.feature.read.generated.resources.mark_as_read
import bibleplanner.feature.read.generated.resources.mark_as_unread
import bibleplanner.feature.read.generated.resources.retry
import bibleplanner.feature.read.generated.resources.unknown_error_occurred
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.feature.read.presentation.component.ReadBottomBar
import com.quare.bibleplanner.feature.read.presentation.component.ReadTopBar
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.component.ResponsiveColumn
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadScreen(
    state: ReadUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (ReadUiEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ReadTopBar(
                state = state,
                isScrolled = isScrolled,
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope,
                onEvent = onEvent,
            )
        },
        bottomBar = {
            ReadBottomBar(
                scrollBehavior = scrollBehavior,
                state = state,
                onEvent = onEvent
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (state) {
                is ReadUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ReadUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            space = 8.dp,
                            alignment = Alignment.CenterVertically,
                        ),
                    ) {
                        val (errorMessage, buttonText) = when (state) {
                            is ReadUiState.Error.ChapterNotFound -> stringResource(
                                resource = Res.string.chapter_not_downloaded_error,
                                stringResource(state.bookStringResource),
                                state.chapterNumber,
                                state.selectedBibleVersionName,
                            ) to stringResource(Res.string.manage_bible_versions)

                            is ReadUiState.Error.Unknown -> stringResource(Res.string.unknown_error_occurred) to
                                stringResource(
                                    Res.string.retry,
                                )
                        }
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = { onEvent(state.errorUiEvent) },
                        ) {
                            Text(text = buttonText)
                        }
                    }
                }

                is ReadUiState.Success -> {
                    ResponsiveColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        lazyListState = listState,
                        maxContentWidth = 600.dp,
                        portraitContent = {
                            responsiveItems(
                                state.verses,
                                key = { "verse-${it.number}" },
                            ) { verse ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = verse.number.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    )
                                    Text(
                                        text = verse.text,
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
