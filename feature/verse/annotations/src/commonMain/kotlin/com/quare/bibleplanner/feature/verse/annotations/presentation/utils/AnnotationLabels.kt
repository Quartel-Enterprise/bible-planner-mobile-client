package com.quare.bibleplanner.feature.verse.annotations.presentation.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.filter_all_books
import bibleplanner.feature.verse.annotations.generated.resources.filter_date
import bibleplanner.feature.verse.annotations.generated.resources.group_today
import bibleplanner.feature.verse.annotations.generated.resources.group_yesterday
import bibleplanner.feature.verse.annotations.generated.resources.period_any
import bibleplanner.feature.verse.annotations.generated.resources.period_custom
import bibleplanner.feature.verse.annotations.generated.resources.period_last_30_days
import bibleplanner.feature.verse.annotations.generated.resources.period_last_7_days
import bibleplanner.feature.verse.annotations.generated.resources.period_today
import bibleplanner.feature.verse.annotations.generated.resources.type_all
import bibleplanner.feature.verse.annotations.generated.resources.type_highlights
import bibleplanner.feature.verse.annotations.generated.resources.type_notes
import bibleplanner.feature.verse.annotations.generated.resources.type_saved
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationDateRange
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationGroupLabel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationPeriod
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationTypeFilter
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val SHORT_MONTH_LENGTH = 3

internal val AnnotationTypeFilter.labelResource: StringResource
    get() = when (this) {
        AnnotationTypeFilter.ALL -> Res.string.type_all
        AnnotationTypeFilter.HIGHLIGHTS -> Res.string.type_highlights
        AnnotationTypeFilter.SAVED -> Res.string.type_saved
        AnnotationTypeFilter.NOTES -> Res.string.type_notes
    }

internal val AnnotationTypeFilter.icon: ImageVector
    get() = when (this) {
        AnnotationTypeFilter.ALL -> Icons.Default.Apps
        AnnotationTypeFilter.HIGHLIGHTS -> Icons.Default.BorderColor
        AnnotationTypeFilter.SAVED -> Icons.Default.Bookmark
        AnnotationTypeFilter.NOTES -> Icons.Default.EditNote
    }

internal val AnnotationPeriod.labelResource: StringResource
    get() = when (this) {
        AnnotationPeriod.ANY -> Res.string.period_any
        AnnotationPeriod.TODAY -> Res.string.period_today
        AnnotationPeriod.LAST_7_DAYS -> Res.string.period_last_7_days
        AnnotationPeriod.LAST_30_DAYS -> Res.string.period_last_30_days
        AnnotationPeriod.CUSTOM -> Res.string.period_custom
    }

@Composable
internal fun bookFilterLabel(bookId: BookId?): String =
    bookId?.getBookName() ?: stringResource(Res.string.filter_all_books)

@Composable
internal fun AnnotationGroupLabel.text(): String = when (this) {
    AnnotationGroupLabel.Today -> stringResource(Res.string.group_today)

    AnnotationGroupLabel.Yesterday -> stringResource(Res.string.group_yesterday)

    is AnnotationGroupLabel.MonthOfYear -> {
        val monthName = stringResource(month.toStringResource()).replaceFirstChar(Char::uppercaseChar)
        year?.let { "$monthName $it" } ?: monthName
    }
}

@Composable
internal fun periodLabel(
    period: AnnotationPeriod,
    customRange: AnnotationDateRange?,
    today: LocalDate,
    isChip: Boolean,
): String = when {
    period == AnnotationPeriod.CUSTOM && customRange != null -> customRange.text(today)
    period == AnnotationPeriod.ANY && isChip -> stringResource(Res.string.filter_date)
    else -> stringResource(period.labelResource)
}

@Composable
private fun AnnotationDateRange.text(today: LocalDate): String {
    val startText = start.shortText(today)
    return if (start == end) startText else "$startText – ${end.shortText(today)}"
}

@Composable
private fun LocalDate.shortText(today: LocalDate): String {
    val monthText = stringResource(month.toStringResource()).take(SHORT_MONTH_LENGTH)
    return if (year == today.year) "$day $monthText" else "$day $monthText $year"
}
