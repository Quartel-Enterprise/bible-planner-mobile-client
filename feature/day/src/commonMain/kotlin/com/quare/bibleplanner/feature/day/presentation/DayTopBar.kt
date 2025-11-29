package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.back
import bibleplanner.feature.day.generated.resources.day_week_title
import bibleplanner.feature.day.generated.resources.loading
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayTopBar(
    uiState: DayUiState,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.back),
                )
            }
        },
        title = {
            when (uiState) {
                is DayUiState.Loaded -> {
                    Text(
                        text = stringResource(
                            Res.string.day_week_title,
                            uiState.day.number,
                            uiState.weekNumber,
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }

                is DayUiState.Loading -> {
                    Text(
                        text = stringResource(Res.string.loading),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
        },
    )
}
