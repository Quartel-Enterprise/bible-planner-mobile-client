package com.quare.bibleplanner.feature.dayreadingcomplete.presentation

import androidx.compose.runtime.Composable
import bibleplanner.feature.day_reading_complete.generated.resources.Res
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_day_label
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LocalDate.toDayReadingCompleteDayLabel(language: Language): String {
    val rawMonthName = stringResource(month.toStringResource())
    val monthName = if (language == Language.ENGLISH) rawMonthName else rawMonthName.lowercase()
    return stringResource(
        Res.string.day_reading_complete_day_label,
        day,
        monthName,
    )
}
