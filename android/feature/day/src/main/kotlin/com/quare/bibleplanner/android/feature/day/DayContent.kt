package com.quare.bibleplanner.android.feature.day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.utils.asStable

// Why: the same width caps as the shared screen's ResponsiveColumn and landscape column.
private val narrowContentMaxWidth = 800.dp
internal val wideReadingMaxWidth = 560.dp
private val askAiFabClearance = 88.dp
private const val DAY_CONTENT_TAG = "day_content" // Why: the baseline profile journeys find the day by this tag.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayContent(
    state: DayUiState,
    isWide: Boolean,
    snackbarHostState: SnackbarHostState,
    transitionScopes: DayTransitionScopes?,
    onEvent: (DayUiEvent) -> Unit,
    studySection: @Composable (DayUiState.Loaded, Modifier) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isWide) Modifier else Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (!isWide && state is DayUiState.Loaded) {
                DayAskAiFab(onClick = { onEvent(DayUiEvent.OnAskAiClick) })
            }
        },
        topBar = {
            if (!isWide) {
                DayTopBar(
                    state = state,
                    transitionScopes = transitionScopes,
                    scrollBehavior = scrollBehavior,
                    onEvent = onEvent,
                )
            }
        },
        // Why: CMP-10789 crashes on unattached insets when Nav3 moves a screen between scenes.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.asStable(),
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .consumeWindowInsets(paddingValues)
            .imePadding()
        when (state) {
            DayUiState.Loading -> DayLoadingSkeleton(
                isWide = isWide,
                onBackClick = { onEvent(DayUiEvent.OnBackClick) },
                modifier = contentModifier,
            )

            is DayUiState.Loaded -> if (isWide) {
                DayWideContent(
                    state = state,
                    transitionScopes = transitionScopes,
                    onEvent = onEvent,
                    modifier = contentModifier,
                )
            } else {
                DayNarrowContent(
                    state = state,
                    transitionScopes = transitionScopes,
                    onEvent = onEvent,
                    studySection = studySection,
                    modifier = contentModifier,
                )
            }
        }
    }
    (state as? DayUiState.Loaded)?.datePickerUiState?.let { datePickerUiState ->
        datePickerUiState.visiblePicker?.let { picker ->
            DayReadDatePicker(
                picker = picker,
                datePickerUiState = datePickerUiState,
                onEvent = onEvent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayTopBar(
    state: DayUiState,
    transitionScopes: DayTransitionScopes?,
    scrollBehavior: TopAppBarScrollBehavior,
    onEvent: (DayUiEvent) -> Unit,
) {
    TopAppBar(
        title = {
            when (state) {
                is DayUiState.Loaded -> DayHeaderTitle(
                    state = state,
                    transitionScopes = transitionScopes,
                )

                DayUiState.Loading -> DayHeaderTitleSkeleton(pulse = rememberDaySkeletonPulse())
            }
        },
        navigationIcon = {
            DayBackButton(onClick = { onEvent(DayUiEvent.OnBackClick) })
        },
        scrollBehavior = scrollBehavior,
        windowInsets = TopAppBarDefaults.windowInsets.asStable(),
    )
}

@Composable
internal fun DayBackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.day_back),
        )
    }
}

@Composable
private fun DayNarrowContent(
    state: DayUiState.Loaded,
    transitionScopes: DayTransitionScopes?,
    onEvent: (DayUiEvent) -> Unit,
    studySection: @Composable (DayUiState.Loaded, Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val day = state.day
    LazyColumn(
        modifier = modifier.testTag(DAY_CONTENT_TAG),
        contentPadding = PaddingValues(bottom = askAiFabClearance),
    ) {
        centeredItem(key = "study") {
            studySection(
                state,
                Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
            )
        }
        centeredItem(key = "read_status") {
            DayReadStatusButton(
                isDayRead = day.isRead,
                onClick = { onEvent(DayUiEvent.OnDayReadToggle) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
            )
        }
        centeredItem(key = "passages") {
            DayPassagesCard(
                passages = day.passages,
                chapterReadStatus = state.chapterReadStatus,
                onEvent = onEvent,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        day.plannedReadDate?.let { plannedReadDate ->
            centeredItem(key = "planned_read_date") {
                DayPlannedReadDateText(
                    plannedReadDate = plannedReadDate,
                    weekNumber = state.weekNumber,
                    dayNumber = day.number,
                    transitionScopes = transitionScopes,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                    ),
                )
            }
        }
        centeredItem(key = "completed_date") {
            DayCompletedDateSection(
                isRead = day.isRead,
                formattedReadDate = state.formattedReadDate,
                onEditClick = { onEvent(DayUiEvent.OnEditDateClick) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        centeredItem(key = "notes") {
            DayNotesSection(
                notes = day.notes.orEmpty(),
                onEvent = onEvent,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

private fun LazyListScope.centeredItem(
    key: String,
    content: @Composable () -> Unit,
) {
    item(key = key) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = narrowContentMaxWidth)
                    .fillMaxWidth(),
            ) {
                content()
            }
        }
    }
}

// Why: on wide screens the day study opens in the panel beside this one, so this column leaves it out.
@Composable
private fun DayWideContent(
    state: DayUiState.Loaded,
    transitionScopes: DayTransitionScopes?,
    onEvent: (DayUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val day = state.day
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = wideReadingMaxWidth)
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            DayWideHeader(
                onBackClick = { onEvent(DayUiEvent.OnBackClick) },
            ) {
                DayHeaderTitle(
                    state = state,
                    transitionScopes = transitionScopes,
                    horizontalAlignment = Alignment.Start,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 16.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 16.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DayPassagesCard(
                    passages = day.passages,
                    chapterReadStatus = state.chapterReadStatus,
                    onEvent = onEvent,
                )
                DayReadStatusButton(
                    isDayRead = day.isRead,
                    onClick = { onEvent(DayUiEvent.OnDayReadToggle) },
                    modifier = Modifier.fillMaxWidth(),
                )
                DayAskAiButton(
                    onClick = { onEvent(DayUiEvent.OnAskAiClick) },
                    modifier = Modifier.fillMaxWidth(),
                )
                day.plannedReadDate?.let { plannedReadDate ->
                    DayPlannedReadDateText(
                        plannedReadDate = plannedReadDate,
                        weekNumber = state.weekNumber,
                        dayNumber = day.number,
                        transitionScopes = transitionScopes,
                    )
                }
                DayCompletedDateSection(
                    isRead = day.isRead,
                    formattedReadDate = state.formattedReadDate,
                    onEditClick = { onEvent(DayUiEvent.OnEditDateClick) },
                )
                DayNotesSection(
                    notes = day.notes.orEmpty(),
                    onEvent = onEvent,
                )
            }
        }
    }
}
