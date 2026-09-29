package com.quare.bibleplanner.feature.verse.annotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.VersionAnnotationCount
import com.quare.bibleplanner.feature.verse.annotations.domain.usecase.impl.ObserveOtherVersionAnnotationCountsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveOtherVersionAnnotationCountsUseCaseTest {
    private val a21 = VersionAnnotationCount(
        bibleVersionId = "A21",
        count = 1,
    )
    private val acf = VersionAnnotationCount(
        bibleVersionId = "ACF",
        count = 4,
    )
    private lateinit var selectedVersion: MutableStateFlow<String>
    private lateinit var useCase: ObserveOtherVersionAnnotationCountsUseCase

    @BeforeTest
    fun setUp() {
        selectedVersion = MutableStateFlow("ACF")
        useCase = ObserveOtherVersionAnnotationCountsUseCase(
            getSelectedVersionIdFlow = { selectedVersion },
            observeVersionAnnotationCounts = { flowOf(listOf(a21, acf)) },
        )
    }

    @Test
    fun `GIVEN marks in the selected and another version WHEN observing THEN keeps only the other version`() = runTest {
        // When
        val counts = useCase().first()

        // Then
        assertEquals(
            expected = listOf(a21),
            actual = counts,
        )
    }

    @Test
    fun `GIVEN marks in two versions WHEN switching to one of them THEN offers the one left behind`() = runTest {
        // When
        selectedVersion.value = "A21"
        val counts = useCase().first()

        // Then
        assertEquals(
            expected = listOf(acf),
            actual = counts,
        )
    }
}
