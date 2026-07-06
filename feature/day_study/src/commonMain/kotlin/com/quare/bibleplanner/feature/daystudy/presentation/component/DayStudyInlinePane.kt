package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel

@Composable
internal fun DayStudyInlinePane(
    study: DayStudyModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        DayStudyHeader(
            passageLabel = study.passageLabel,
            modifier = Modifier.padding(
                start = 20.dp,
                top = 16.dp,
                end = 12.dp,
                bottom = 4.dp,
            ),
        )
        DayStudyTabbedContent(
            study = study,
            modifier = Modifier.weight(1f),
            tabRowModifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 8.dp,
            ),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 10.dp,
                end = 20.dp,
                bottom = 24.dp,
            ),
        )
    }
}
