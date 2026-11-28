package com.quare.bibleplanner.android.feature.readingplan

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource

private const val SHORT_MONTH_LENGTH = 3

@Composable
internal fun Month.toShortMonthLabel(): String = stringResource(toStringResource()).take(SHORT_MONTH_LENGTH)

@Composable
internal fun LocalDate.toShortDateLabel(): String = "${day.toDayLabel()} ${month.toShortMonthLabel()}"

internal fun Int.toDayLabel(): String = toString().padStart(2, '0')
