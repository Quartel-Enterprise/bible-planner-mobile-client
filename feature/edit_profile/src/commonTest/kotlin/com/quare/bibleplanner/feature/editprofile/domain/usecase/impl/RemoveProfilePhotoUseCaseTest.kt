package com.quare.bibleplanner.feature.editprofile.domain.usecase.impl

import com.quare.bibleplanner.feature.editprofile.fake.FakeProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RemoveProfilePhotoUseCaseTest {
    private lateinit var repository: FakeProfileRepository
    private lateinit var useCase: RemoveProfilePhotoUseCase

    @Test
    fun `GIVEN a profile photo WHEN removing it THEN asks the repository to remove the photo`() = runTest {
        // When
        useCase()

        // Then
        assertEquals(1, repository.removePhotoCalls)
    }

    @BeforeTest
    fun setUp() {
        repository = FakeProfileRepository()
        useCase = RemoveProfilePhotoUseCase(profileRepository = repository)
    }
}
