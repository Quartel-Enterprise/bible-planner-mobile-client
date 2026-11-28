package com.quare.bibleplanner.android.feature.annotations

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.date.dateRepresentationLabel
import com.quare.bibleplanner.android.designsystem.date.rememberMonthLabel
import com.quare.bibleplanner.android.designsystem.date.rememberShortDateLabel
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.date.DateRepresentation
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationDateRange
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationGroupLabel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationPeriod
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationTypeFilter
import kotlinx.datetime.LocalDate

@StringRes
internal fun AnnotationTypeFilter.toLabelRes(): Int = when (this) {
    AnnotationTypeFilter.ALL -> R.string.annotations_type_all
    AnnotationTypeFilter.HIGHLIGHTS -> R.string.annotations_type_highlights
    AnnotationTypeFilter.SAVED -> R.string.annotations_type_saved
    AnnotationTypeFilter.NOTES -> R.string.annotations_type_notes
}

internal fun AnnotationTypeFilter.toIcon(): ImageVector = when (this) {
    AnnotationTypeFilter.ALL -> Icons.Default.Apps
    AnnotationTypeFilter.HIGHLIGHTS -> Icons.Default.BorderColor
    AnnotationTypeFilter.SAVED -> Icons.Default.Bookmark
    AnnotationTypeFilter.NOTES -> Icons.Default.EditNote
}

@StringRes
internal fun AnnotationPeriod.toLabelRes(): Int = when (this) {
    AnnotationPeriod.ANY -> R.string.annotations_period_any
    AnnotationPeriod.TODAY -> R.string.annotations_period_today
    AnnotationPeriod.LAST_7_DAYS -> R.string.annotations_period_last_7_days
    AnnotationPeriod.LAST_30_DAYS -> R.string.annotations_period_last_30_days
    AnnotationPeriod.CUSTOM -> R.string.annotations_period_custom
}

@StringRes
internal fun HighlightColor.toNameRes(): Int = when (this) {
    is HighlightColor.Preset -> when (preset) {
        PresetHighlightColor.TEAL -> R.string.annotations_color_teal
        PresetHighlightColor.GREEN -> R.string.annotations_color_green
        PresetHighlightColor.YELLOW -> R.string.annotations_color_yellow
        PresetHighlightColor.PINK -> R.string.annotations_color_pink
        PresetHighlightColor.ORANGE -> R.string.annotations_color_orange
        PresetHighlightColor.PURPLE -> R.string.annotations_color_purple
    }

    is HighlightColor.Custom -> R.string.annotations_color_custom
}

@Composable
internal fun bookFilterLabel(bookId: BookId?): String =
    bookId?.getBookName() ?: stringResource(R.string.annotations_filter_all_books)

@Composable
internal fun AnnotationGroupLabel.text(): String = when (this) {
    AnnotationGroupLabel.Today -> dateRepresentationLabel(DateRepresentation.Today)

    AnnotationGroupLabel.Yesterday -> dateRepresentationLabel(DateRepresentation.Yesterday)

    is AnnotationGroupLabel.MonthOfYear -> rememberMonthLabel(
        month = month,
        year = year,
    )
}

@Composable
internal fun periodLabel(
    period: AnnotationPeriod,
    customRange: AnnotationDateRange?,
    today: LocalDate,
    isChip: Boolean,
): String = when {
    period == AnnotationPeriod.CUSTOM && customRange != null -> customRange.text(today)
    period == AnnotationPeriod.ANY && isChip -> stringResource(R.string.annotations_filter_date)
    else -> stringResource(period.toLabelRes())
}

@Composable
private fun AnnotationDateRange.text(today: LocalDate): String {
    val startText = start.shortText(today)
    return if (start == end) startText else "$startText – ${end.shortText(today)}"
}

@Composable
private fun LocalDate.shortText(today: LocalDate): String = rememberShortDateLabel(
    date = this,
    isYearShown = year != today.year,
)
