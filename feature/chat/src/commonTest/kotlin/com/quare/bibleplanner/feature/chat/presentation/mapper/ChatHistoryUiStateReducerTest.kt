package com.quare.bibleplanner.feature.chat.presentation.mapper

import com.quare.bibleplanner.feature.chat.presentation.model.ChatHistoryUiState
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ChatHistoryUiStateReducerTest {
    private val history = ChatHistoryUiState(
        isOpen = true,
        query = "",
        groups = emptyList(),
        hasConversations = true,
        expandedActionsId = null,
        renamingId = null,
        renameDraft = "",
        deletingId = null,
    )

    @Test
    fun `GIVEN closed actions WHEN toggling a conversation THEN its actions open`() {
        // Given
        val state = history

        // When
        val toggled = state.toggleActionsOf(FIRST_ID)

        // Then
        assertEquals(state.copy(expandedActionsId = FIRST_ID), toggled)
    }

    @Test
    fun `GIVEN open actions WHEN toggling the same conversation THEN its actions close`() {
        // Given
        val state = history.copy(expandedActionsId = FIRST_ID)

        // When
        val toggled = state.toggleActionsOf(FIRST_ID)

        // Then
        assertEquals(history, toggled)
    }

    @Test
    fun `GIVEN open actions WHEN toggling another conversation THEN the actions move to it`() {
        // Given
        val state = history.copy(expandedActionsId = FIRST_ID)

        // When
        val toggled = state.toggleActionsOf(SECOND_ID)

        // Then
        assertEquals(history.copy(expandedActionsId = SECOND_ID), toggled)
    }

    @Test
    fun `GIVEN a rename and a deletion in progress WHEN toggling actions THEN both are dropped`() {
        // Given
        val state = history.copy(
            renamingId = FIRST_ID,
            renameDraft = TITLE,
            deletingId = SECOND_ID,
        )

        // When
        val toggled = state.toggleActionsOf(FIRST_ID)

        // Then
        val expected = state.copy(
            expandedActionsId = FIRST_ID,
            renamingId = null,
            deletingId = null,
        )
        assertEquals(expected, toggled)
    }

    @Test
    fun `GIVEN a deletion in progress WHEN starting a rename THEN the rename starts with the title`() {
        // Given
        val state = history.copy(
            expandedActionsId = FIRST_ID,
            deletingId = SECOND_ID,
        )

        // When
        val renaming = state.startRename(
            conversationId = FIRST_ID,
            title = TITLE,
        )

        // Then
        val expected = history.copy(
            expandedActionsId = FIRST_ID,
            renamingId = FIRST_ID,
            renameDraft = TITLE,
        )
        assertEquals(expected, renaming)
    }

    @Test
    fun `GIVEN a rename in progress WHEN cancelling it THEN the actions stay open`() {
        // Given
        val state = history.copy(
            expandedActionsId = FIRST_ID,
            renamingId = FIRST_ID,
            renameDraft = TITLE,
        )

        // When
        val cancelled = state.cancelRename()

        // Then
        assertEquals(history.copy(expandedActionsId = FIRST_ID), cancelled)
    }

    @Test
    fun `GIVEN a rename in progress WHEN closing it THEN the actions close too`() {
        // Given
        val state = history.copy(
            expandedActionsId = FIRST_ID,
            renamingId = FIRST_ID,
            renameDraft = TITLE,
        )

        // When
        val closed = state.closeRename()

        // Then
        assertEquals(history, closed)
    }

    @Test
    fun `GIVEN a deletion in progress WHEN closing it THEN the deletion and the actions close`() {
        // Given
        val state = history.copy(
            expandedActionsId = FIRST_ID,
            deletingId = FIRST_ID,
        )

        // When
        val closed = state.closeDelete()

        // Then
        assertEquals(history, closed)
    }

    private companion object {
        const val FIRST_ID = "conversation-1"
        const val SECOND_ID = "conversation-2"
        const val TITLE = "Caim e Abel"
    }
}
