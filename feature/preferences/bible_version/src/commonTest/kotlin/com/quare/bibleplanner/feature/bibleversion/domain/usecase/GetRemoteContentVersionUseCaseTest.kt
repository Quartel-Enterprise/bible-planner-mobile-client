package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.testing.FakeBibleVersionRepository
import com.quare.bibleplanner.core.utils.locale.Language
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
    fun `GIVEN an uppercase remote id WHEN reading it in lowercase THEN returns its content version`() = runTest {
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
    fun `GIVEN a version not listed remotely WHEN reading it THEN returns an empty version`() = runTest {
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
    fun `GIVEN a remote list that cannot be loaded WHEN reading a version THEN returns an empty version`() = runTest {
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
