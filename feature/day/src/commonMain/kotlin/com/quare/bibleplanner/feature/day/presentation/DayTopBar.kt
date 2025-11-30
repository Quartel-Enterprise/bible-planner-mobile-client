package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.day_week_title
import bibleplanner.feature.day.generated.resources.loading
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.component.backicon.BackIcon
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayTopBar(
    uiState: DayUiState,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            BackIcon(onBackClick = onBackClick)
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
                    )
                }

                is DayUiState.Loading -> {
                    Text(
                        text = stringResource(Res.string.loading),
                    )
                }
            }
        },
    )
}
