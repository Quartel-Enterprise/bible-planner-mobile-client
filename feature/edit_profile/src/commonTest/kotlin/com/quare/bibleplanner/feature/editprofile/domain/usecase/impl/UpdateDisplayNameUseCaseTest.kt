package com.quare.bibleplanner.feature.editprofile.domain.usecase.impl

import com.quare.bibleplanner.feature.editprofile.fake.FakeProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class UpdateDisplayNameUseCaseTest {
    private lateinit var repository: FakeProfileRepository
    private lateinit var useCase: UpdateDisplayNameUseCase

    @Test
    fun `GIVEN a name with surrounding spaces WHEN updating THEN saves the trimmed name`() = runTest {
        // When
        useCase("  Maria  ")

        // Then
        assertEquals(listOf("Maria"), repository.displayNames)
    }

    @Test
    fun `GIVEN a blank name WHEN updating THEN saves nothing`() = runTest {
        // When
        useCase("   ")

        // Then
        assertTrue(repository.displayNames.isEmpty())
    }

    @BeforeTest
    fun setUp() {
        repository = FakeProfileRepository()
        useCase = UpdateDisplayNameUseCase(profileRepository = repository)
    }
}
