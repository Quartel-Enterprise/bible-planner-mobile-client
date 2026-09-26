package com.quare.bibleplanner.feature.editprofile.domain.usecase.impl

import com.quare.bibleplanner.feature.editprofile.fake.FakeProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class UseProviderPhotoUseCaseTest {
    private lateinit var repository: FakeProfileRepository
    private lateinit var useCase: UseProviderPhotoUseCase

    @Test
    fun `GIVEN a sign-in provider photo WHEN falling back to it THEN asks the repository to use the provider photo`() =
        runTest {
            // When
            useCase()

            // Then
            assertEquals(1, repository.useProviderPhotoCalls)
        }

    @BeforeTest
    fun setUp() {
        repository = FakeProfileRepository()
        useCase = UseProviderPhotoUseCase(profileRepository = repository)
    }
}
