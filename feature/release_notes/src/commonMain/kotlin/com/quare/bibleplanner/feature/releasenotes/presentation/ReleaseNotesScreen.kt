package com.quare.bibleplanner.feature.releasenotes.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.release_notes.generated.resources.Res
import bibleplanner.feature.release_notes.generated.resources.release_notes_error_loading
import bibleplanner.feature.release_notes.generated.resources.release_notes_screen_title
import bibleplanner.feature.release_notes.generated.resources.release_notes_tab_latest
import bibleplanner.feature.release_notes.generated.resources.release_notes_tab_past
import bibleplanner.feature.release_notes.generated.resources.release_notes_tab_upcoming
import bibleplanner.feature.release_notes.generated.resources.release_notes_view_all_github
import bibleplanner.ui.component.generated.resources.ic_github
import com.quare.bibleplanner.feature.releasenotes.presentation.component.ReleaseNoteCard
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiEvent
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState
import com.quare.bibleplanner.ui.component.ResponsiveColumn
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import bibleplanner.ui.component.generated.resources.Res as ComponentRes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ReleaseNotesScreen(
    uiState: ReleaseNotesUiState,
    onEvent: (ReleaseNotesUiEvent) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    with(sharedTransitionScope) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.release_notes_screen_title),
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "release_notes_card"),
                                animatedVisibilityScope = animatedContentScope,
                            ),
                        )
                    },
                    navigationIcon = {
                        BackIcon(onBackClick = { onEvent(ReleaseNotesUiEvent.OnBackClicked) })
                    },
                    actions = {
                        CommonIconButton(
                            painter = painterResource(ComponentRes.drawable.ic_github),
                            contentDescription = stringResource(Res.string.release_notes_view_all_github),
                            onClick = { onEvent(ReleaseNotesUiEvent.OnGithubAllReleasesClicked) },
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { paddingValues ->
            ResponsiveColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                portraitContent = {
                    when (uiState) {
                        ReleaseNotesUiState.Loading -> {
                            responsiveItem {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        ReleaseNotesUiState.Error -> {
                            responsiveItem {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.release_notes_error_loading),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }

                        is ReleaseNotesUiState.Success -> {
                            responsiveItem {
                                // Tabs
                                val availableTabs = uiState.availableTabs
                                val selectedTabIndex = availableTabs.indexOf(uiState.currentTab).coerceAtLeast(0)

                                SecondaryTabRow(
                                    selectedTabIndex = selectedTabIndex,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    indicator = {
                                        TabRowDefaults.SecondaryIndicator(
                                            Modifier.tabIndicatorOffset(selectedTabIndex),
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    },
                                ) {
                                    availableTabs.forEach { tab ->
                                        val (textRes, tabEvent) = when (tab) {
                                            ReleaseNotesTab.LATEST -> {
                                                Res.string.release_notes_tab_latest to
                                                    ReleaseNotesTab.LATEST
                                            }

                                            ReleaseNotesTab.UPCOMING -> {
                                                Res.string.release_notes_tab_upcoming to
                                                    ReleaseNotesTab.UPCOMING
                                            }

                                            ReleaseNotesTab.PAST_VERSIONS -> {
                                                Res.string.release_notes_tab_past to
                                                    ReleaseNotesTab.PAST_VERSIONS
                                            }
                                        }
                                        Tab(
                                            selected = uiState.currentTab == tab,
                                            onClick = { onEvent(ReleaseNotesUiEvent.OnTabSelected(tabEvent)) },
                                            text = { Text(stringResource(textRes)) },
                                        )
                                    }
                                }
                            }

                            when (uiState.currentTab) {
                                ReleaseNotesTab.LATEST -> {
                                    uiState.latestRelease?.let { note ->
                                        responsiveItem {
                                            ReleaseNoteCard(
                                                note = note,
                                                isLatest = true,
                                                onGithubClick = {
                                                    onEvent(
                                                        ReleaseNotesUiEvent.OnGithubVersionClicked(it),
                                                    )
                                                },
                                            )
                                        }
                                    }
                                }

                                ReleaseNotesTab.UPCOMING -> {
                                    responsiveItems(uiState.upcomingReleases) { note ->
                                        ReleaseNoteCard(
                                            note = note,
                                            isUpcoming = true,
                                            onGithubClick = { onEvent(ReleaseNotesUiEvent.OnGithubVersionClicked(it)) },
                                        )
                                    }
                                }

                                ReleaseNotesTab.PAST_VERSIONS -> {
                                    responsiveItems(uiState.pastReleases) { note ->
                                        ReleaseNoteCard(
                                            note = note,
                                            isLatest = false,
                                            onGithubClick = { onEvent(ReleaseNotesUiEvent.OnGithubVersionClicked(it)) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
            )
        }
    }
}
