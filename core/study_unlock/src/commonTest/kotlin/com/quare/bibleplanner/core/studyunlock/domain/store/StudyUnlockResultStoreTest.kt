package com.quare.bibleplanner.core.studyunlock.domain.store

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class StudyUnlockResultStoreTest {
    private lateinit var store: StudyUnlockResultStore

    @BeforeTest
    fun setUp() {
        store = StudyUnlockResultStore()
    }

    @Test
    fun `GIVEN an observer WHEN its request is earned THEN it is notified once`() =
        runTest(UnconfinedTestDispatcher()) {
            // Given
            var notifications = 0
            backgroundScope.launch { store.observeEarned(REQUEST_KEY).collect { notifications++ } }

            // When
            store.publishEarned(REQUEST_KEY)

            // Then
            assertEquals(1, notifications)
        }

    @Test
    fun `GIVEN an earned request WHEN an observer arrives later THEN it still receives it`() =
        runTest(UnconfinedTestDispatcher()) {
            // Given
            var notifications = 0
            store.publishEarned(REQUEST_KEY)

            // When
            backgroundScope.launch { store.observeEarned(REQUEST_KEY).collect { notifications++ } }

            // Then
            assertEquals(1, notifications)
        }

    @Test
    fun `GIVEN another request WHEN it is earned THEN the observer is not notified`() =
        runTest(UnconfinedTestDispatcher()) {
            // Given
            var notifications = 0
            backgroundScope.launch { store.observeEarned(REQUEST_KEY).collect { notifications++ } }

            // When
            store.publishEarned("other")

            // Then
            assertEquals(0, notifications)
        }

    private companion object {
        const val REQUEST_KEY = "chapter_study|GEN|3"
    }
}
