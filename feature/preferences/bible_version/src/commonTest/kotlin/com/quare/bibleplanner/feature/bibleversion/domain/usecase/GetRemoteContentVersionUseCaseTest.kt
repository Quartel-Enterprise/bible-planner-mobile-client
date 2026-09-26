package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.bibleversion.fake.FakeBibleVersionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetRemoteContentVersionUseCaseTest {
    private val remoteVersion = VersionModel(
        id = "ACF",
        name = "Almeida Corrigida Fiel",
        version = "2.0.0",
        language = Language.PORTUGUESE_BRAZIL,
        chapters = 1189,
        size = null,
    )

    @Test
    fun `reads the remote content version matching the id whatever its case`() = runTest {
        // Given
        val useCase = GetRemoteContentVersionUseCase(FakeBibleVersionRepository(Result.success(listOf(remoteVersion))))

        // When
        val contentVersion = useCase("acf")

        // Then
        assertEquals(
            expected = "2.0.0",
            actual = contentVersion,
        )
    }

    @Test
    fun `returns an empty version when the version is not listed remotely`() = runTest {
        // Given
        val useCase = GetRemoteContentVersionUseCase(FakeBibleVersionRepository(Result.success(listOf(remoteVersion))))

        // When
        val contentVersion = useCase("kjv")

        // Then
        assertEquals(
            expected = "",
            actual = contentVersion,
        )
    }

    @Test
    fun `returns an empty version when the remote list cannot be loaded`() = runTest {
        // Given
        val useCase = GetRemoteContentVersionUseCase(
            FakeBibleVersionRepository(Result.failure(IllegalStateException("offline"))),
        )

        // When
        val contentVersion = useCase("acf")

        // Then
        assertEquals(
            expected = "",
            actual = contentVersion,
        )
    }
}
