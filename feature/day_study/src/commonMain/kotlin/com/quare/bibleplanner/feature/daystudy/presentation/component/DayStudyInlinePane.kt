package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel

private val contentMaxWidth = 720.dp

@Composable
internal fun DayStudyInlinePane(
    study: DayStudyModel,
    showHeader: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showHeader) {
            DayStudyHeader(
                passageLabel = study.passageLabel,
                showIcon = true,
                modifier = Modifier
                    .widthIn(max = contentMaxWidth)
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        top = 16.dp,
                        end = 20.dp,
                        bottom = 12.dp,
                    ),
            )
        }
        DayStudyTabbedContent(
            study = study,
            contentMaxWidth = contentMaxWidth,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 10.dp,
                end = 20.dp,
                bottom = 24.dp,
            ),
        )
    }
}
