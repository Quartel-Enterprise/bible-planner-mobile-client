package com.quare.bibleplanner.android.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.date.dateRepresentationLabel
import com.quare.bibleplanner.core.date.DateRepresentation
import com.quare.bibleplanner.feature.chat.presentation.model.ChatConversationBucket
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
internal fun bucketLabel(bucket: ChatConversationBucket): String = when (bucket) {
    ChatConversationBucket.Today -> dateRepresentationLabel(DateRepresentation.Today)
    ChatConversationBucket.Yesterday -> dateRepresentationLabel(DateRepresentation.Yesterday)
    ChatConversationBucket.LastSevenDays -> stringResource(R.string.chat_bucket_last_seven_days)
    is ChatConversationBucket.InMonth -> monthBucketLabel(bucket)
}

// Why: a month of an earlier year would otherwise read the same as that month of this year.
@Composable
private fun monthBucketLabel(bucket: ChatConversationBucket.InMonth): String {
    val month = stringResource(bucket.month.toStringRes())
    val currentYear = remember {
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .year
    }
    return if (bucket.year == currentYear) {
        month
    } else {
        stringResource(
            R.string.chat_bucket_month_year,
            month,
            bucket.year,
        )
    }
}

private fun Month.toStringRes(): Int = when (this) {
    Month.JANUARY -> R.string.chat_january
    Month.FEBRUARY -> R.string.chat_february
    Month.MARCH -> R.string.chat_march
    Month.APRIL -> R.string.chat_april
    Month.MAY -> R.string.chat_may
    Month.JUNE -> R.string.chat_june
    Month.JULY -> R.string.chat_july
    Month.AUGUST -> R.string.chat_august
    Month.SEPTEMBER -> R.string.chat_september
    Month.OCTOBER -> R.string.chat_october
    Month.NOVEMBER -> R.string.chat_november
    Month.DECEMBER -> R.string.chat_december
}
