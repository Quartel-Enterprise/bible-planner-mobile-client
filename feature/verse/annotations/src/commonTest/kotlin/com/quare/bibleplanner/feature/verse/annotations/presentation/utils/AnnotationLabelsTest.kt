package com.quare.bibleplanner.feature.verse.annotations.presentation.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.EditNote
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.period_any
import bibleplanner.feature.verse.annotations.generated.resources.period_custom
import bibleplanner.feature.verse.annotations.generated.resources.period_last_30_days
import bibleplanner.feature.verse.annotations.generated.resources.period_last_7_days
import bibleplanner.feature.verse.annotations.generated.resources.period_today
import bibleplanner.feature.verse.annotations.generated.resources.type_all
import bibleplanner.feature.verse.annotations.generated.resources.type_highlights
import bibleplanner.feature.verse.annotations.generated.resources.type_notes
import bibleplanner.feature.verse.annotations.generated.resources.type_saved
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationPeriod
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationTypeFilter
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AnnotationLabelsTest {
    @Test
    fun `GIVEN every annotation type WHEN reading its label and icon THEN labels and draws each type`() {
        // Given
        val types = AnnotationTypeFilter.entries

        // When
        val labels = types.associateWith { it.labelResource to it.icon }

        // Then
        assertEquals(
            expected = mapOf(
                AnnotationTypeFilter.ALL to (Res.string.type_all to Icons.Default.Apps),
                AnnotationTypeFilter.HIGHLIGHTS to (Res.string.type_highlights to Icons.Default.BorderColor),
                AnnotationTypeFilter.SAVED to (Res.string.type_saved to Icons.Default.Bookmark),
                AnnotationTypeFilter.NOTES to (Res.string.type_notes to Icons.Default.EditNote),
            ),
            actual = labels,
        )
    }

    @Test
    fun `GIVEN every period WHEN reading its label THEN labels each period`() {
        // Given
        val periods = AnnotationPeriod.entries

        // When
        val labels = periods.associateWith { it.labelResource }

        // Then
        assertEquals(
            expected = mapOf(
                AnnotationPeriod.ANY to Res.string.period_any,
                AnnotationPeriod.TODAY to Res.string.period_today,
                AnnotationPeriod.LAST_7_DAYS to Res.string.period_last_7_days,
                AnnotationPeriod.LAST_30_DAYS to Res.string.period_last_30_days,
                AnnotationPeriod.CUSTOM to Res.string.period_custom,
            ),
            actual = labels,
        )
    }
}
