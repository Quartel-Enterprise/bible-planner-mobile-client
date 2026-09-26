package com.quare.bibleplanner.feature.chat.domain.coordinator

import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.feature.chat.domain.repository.FakeChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ChatSyncCoordinatorImplTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var coordinator: ChatSyncCoordinatorImpl
    private lateinit var repository: FakeChatRepository

    @BeforeTest
    fun setUp() {
        repository = FakeChatRepository()
        coordinator = ChatSyncCoordinatorImpl(
            applicationScope = ApplicationScope(CoroutineScope(testDispatcher)),
            repository = repository,
        )
    }

    @Test
    fun `GIVEN a stopped sync WHEN starting it THEN syncs the remote changes`() = runTest(testDispatcher) {
        // When
        coordinator.ensureStarted()
        runCurrent()

        // Then
        assertEquals(
            expected = 1,
            actual = repository.syncCount,
        )
    }

    @Test
    fun `GIVEN a running sync WHEN starting it again THEN keeps a single sync`() = runTest(testDispatcher) {
        // Given
        coordinator.ensureStarted()

        // When
        coordinator.ensureStarted()
        runCurrent()

        // Then
        assertEquals(
            expected = 1,
            actual = repository.syncCount,
        )
    }
}
