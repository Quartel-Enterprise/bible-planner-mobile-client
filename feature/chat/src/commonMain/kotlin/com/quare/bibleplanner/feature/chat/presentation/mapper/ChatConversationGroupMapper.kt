package com.quare.bibleplanner.feature.chat.presentation.mapper

import com.quare.bibleplanner.core.date.toRelativeTime
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.presentation.model.ChatConversationBucket
import com.quare.bibleplanner.feature.chat.presentation.model.ChatConversationGroupUiModel
import com.quare.bibleplanner.feature.chat.presentation.model.ChatConversationUiModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

private const val RECENT_DAYS = 7

class ChatConversationGroupMapper {
    fun map(
        conversations: List<ChatConversationModel>,
        activeConversationId: String?,
        query: String,
        now: Instant = Clock.System.now(),
    ): List<ChatConversationGroupUiModel> {
        val timeZone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(timeZone).date
        return conversations
            .filter { conversation -> conversation.matches(query) }
            .groupBy { conversation ->
                bucketOf(
                    date = conversation.updatedAt.toLocalDateTime(timeZone).date,
                    today = today,
                )
            }.map { (bucket, grouped) ->
                ChatConversationGroupUiModel(
                    bucket = bucket,
                    conversations = grouped.map { conversation ->
                        conversation.toUiModel(
                            activeConversationId = activeConversationId,
                            now = now,
                        )
                    },
                )
            }
    }

    private fun ChatConversationModel.matches(query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return true
        return title.contains(trimmed, ignoreCase = true) || preview?.contains(trimmed, ignoreCase = true) == true
    }

    private fun ChatConversationModel.toUiModel(
        activeConversationId: String?,
        now: Instant,
    ): ChatConversationUiModel = ChatConversationUiModel(
        id = id,
        title = title,
        preview = preview,
        relativeTime = updatedAt.toRelativeTime(now),
        isActive = id == activeConversationId,
    )

    private fun bucketOf(
        date: LocalDate,
        today: LocalDate,
    ): ChatConversationBucket = when {
        date == today -> ChatConversationBucket.Today

        date == today.minus(1, DateTimeUnit.DAY) -> ChatConversationBucket.Yesterday

        date > today.minus(RECENT_DAYS, DateTimeUnit.DAY) -> ChatConversationBucket.LastSevenDays

        else -> ChatConversationBucket.InMonth(
            month = date.month,
            year = date.year,
        )
    }
}
