package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.ui.component.ResponsiveContentScope

private const val DAY_PASSAGES_TAG = "day_passages"

internal fun ResponsiveContentScope.portraitPassageList(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    passages: List<PassageModel>,
    chapterReadStatus: Map<Pair<Int, Int>, Boolean>,
    onEvent: (DayUiEvent) -> Unit,
) {
    responsiveItem(key = "passage_list_card") {
        ElevatedCard(
            modifier = Modifier
                .testTag(DAY_PASSAGES_TAG)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            PassageRowsColumn(
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                passages = passages,
                chapterReadStatus = chapterReadStatus,
                itemStartPadding = 16.dp,
                onEvent = onEvent,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
