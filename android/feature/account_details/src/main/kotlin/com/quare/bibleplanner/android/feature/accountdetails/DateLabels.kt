package com.quare.bibleplanner.android.feature.accountdetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.quare.bibleplanner.android.designsystem.date.relativeDateTimeLabel
import com.quare.bibleplanner.android.designsystem.date.rememberMediumDateLabel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
internal fun rememberLastLoginLabel(instant: Instant): String = relativeDateTimeLabel(rememberLocalDateTime(instant))

@Composable
internal fun rememberCreatedAtLabel(instant: Instant): String =
    rememberMediumDateLabel(rememberLocalDateTime(instant).date)

@Composable
private fun rememberLocalDateTime(instant: Instant): LocalDateTime =
    remember(instant) { instant.toLocalDateTime(TimeZone.currentSystemDefault()) }
