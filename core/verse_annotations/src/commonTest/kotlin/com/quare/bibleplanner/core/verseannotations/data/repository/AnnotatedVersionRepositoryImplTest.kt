package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.verseannotations.fake.FakeAnnotatedVersionDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AnnotatedVersionRepositoryImplTest {
    private lateinit var dao: FakeAnnotatedVersionDao

    @Test
    fun `GIVEN annotated versions WHEN observing them THEN lists them in a stable order`() = runTest {
        // Given
        val repository = prepareScenario(initialVersionIds = listOf("NVI", "A21", "ACF"))

        // When
        val versionIds = repository.observeAnnotatedVersionIds().first()

        // Then
        assertEquals(
            expected = listOf("A21", "ACF", "NVI"),
            actual = versionIds,
        )
    }

    @Test
    fun `GIVEN annotated versions WHEN the database reorders the same versions THEN emits them once`() = runTest {
        // Given
        val repository = prepareScenario(initialVersionIds = listOf("A21", "ACF"))
        val emissions = mutableListOf<List<String>>()
        backgroundScope.launch { repository.observeAnnotatedVersionIds().toList(emissions) }
        runCurrent()

        // When
        dao.versionIds.value = listOf("ACF", "A21")
        runCurrent()
        dao.versionIds.value = listOf("ACF", "A21", "NVI")
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(
                listOf("A21", "ACF"),
                listOf("A21", "ACF", "NVI"),
            ),
            actual = emissions,
        )
    }

    private fun prepareScenario(initialVersionIds: List<String>): AnnotatedVersionRepositoryImpl {
        dao = FakeAnnotatedVersionDao(initialVersionIds = initialVersionIds)
        return AnnotatedVersionRepositoryImpl(annotatedVersionDao = dao)
    }
}
