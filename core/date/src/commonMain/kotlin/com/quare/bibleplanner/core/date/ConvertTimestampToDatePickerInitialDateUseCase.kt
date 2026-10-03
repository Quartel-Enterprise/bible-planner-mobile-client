package com.quare.bibleplanner.core.date

class ConvertTimestampToDatePickerInitialDateUseCase(
    private val localDateTimeProvider: LocalDateTimeProvider,
) {
    // Why: the DatePicker reads initialSelectedDateMillis as a UTC date, so the local date must be sent as its UTC
    // midnight; its local midnight falls on the previous UTC day east of UTC and preselects the wrong day.
    operator fun invoke(timestamp: Long): Long = localDateTimeProvider
        .getLocalDateTime(timestamp)
        .toLocalDate()
        .toTimestampUTC()
}
