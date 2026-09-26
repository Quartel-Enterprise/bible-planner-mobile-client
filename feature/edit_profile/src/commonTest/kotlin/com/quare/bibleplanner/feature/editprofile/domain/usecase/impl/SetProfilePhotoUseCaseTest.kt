package com.quare.bibleplanner.feature.editprofile.domain.usecase.impl

import com.quare.bibleplanner.feature.editprofile.fake.FakeProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class SetProfilePhotoUseCaseTest {
    private lateinit var repository: FakeProfileRepository
    private lateinit var useCase: SetProfilePhotoUseCase

    @Test
    fun `GIVEN encoded photo bytes WHEN setting the photo THEN hands the bytes to the repository`() = runTest {
        // When
        useCase(byteArrayOf(1, 2, 3))

        // Then
        assertEquals(1, repository.photos.size)
        assertContentEquals(byteArrayOf(1, 2, 3), repository.photos.single())
    }

    @BeforeTest
    fun setUp() {
        repository = FakeProfileRepository()
        useCase = SetProfilePhotoUseCase(profileRepository = repository)
    }
}
