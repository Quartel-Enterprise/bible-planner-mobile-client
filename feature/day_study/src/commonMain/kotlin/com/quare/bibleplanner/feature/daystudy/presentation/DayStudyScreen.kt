package com.quare.bibleplanner.feature.daystudy.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_title
import com.quare.bibleplanner.feature.daystudy.presentation.component.DayStudyPane
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiState
import com.quare.bibleplanner.ui.component.icon.BackIcon
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayStudyScreen(
    uiState: DayStudyRouteUiState,
    isWide: Boolean,
    snackbarHostState: SnackbarHostState,
    onCardClick: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    if (isWide) {
        DayStudyPane(
            cardState = uiState.card,
            openStudy = uiState.openStudy,
            generation = uiState.generation,
            isOpeningStudy = uiState.isOpeningStudy,
            onCardClick = onCardClick,
            showStudyHeader = true,
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { DayStudyTopBarTitle(passageLabel = uiState.passageLabel) },
                    navigationIcon = {
                        BackIcon(platform = uiState.platform, onBackClick = onNavigateBack)
                    },
                )
            },
        ) { paddingValues ->
            DayStudyPane(
                cardState = uiState.card,
                openStudy = uiState.openStudy,
                generation = uiState.generation,
                isOpeningStudy = uiState.isOpeningStudy,
                onCardClick = onCardClick,
                modifier = Modifier.padding(paddingValues),
                showStudyHeader = false,
            )
        }
    }
}

@Composable
private fun DayStudyTopBarTitle(passageLabel: String?) {
    Column {
        Text(
            text = stringResource(Res.string.ai_study_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        passageLabel?.let { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
