package com.quare.bibleplanner.feature.chat.domain.usecase.impl

import com.quare.bibleplanner.feature.chat.data.datasource.FakeChatLocalDataSource
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Instant

internal class ClearChatLocalDataUseCaseTest {
    @Test
    fun `GIVEN cached conversations WHEN clearing the local data THEN the cache is emptied`() = runTest {
        // Given
        val localDataSource = FakeChatLocalDataSource()
        localDataSource.conversations.value = listOf(
            ChatConversationModel(
                id = "conversation-1",
                title = "Caim e Abel",
                preview = null,
                contextLabel = null,
                planDay = null,
                updatedAt = Instant.parse("2026-08-14T12:00:00Z"),
            ),
        )

        // When
        ClearChatLocalDataUseCase(localDataSource)()

        // Then
        assertTrue(localDataSource.conversations.value.isEmpty())
    }
}
