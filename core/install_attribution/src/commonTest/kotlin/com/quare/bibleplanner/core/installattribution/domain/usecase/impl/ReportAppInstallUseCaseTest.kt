package com.quare.bibleplanner.core.installattribution.domain.usecase.impl

import com.quare.bibleplanner.core.installattribution.data.mapper.InstallReferrerParser
import com.quare.bibleplanner.core.installattribution.domain.model.InstallReport
import com.quare.bibleplanner.core.installattribution.domain.model.InstallReportOutcome
import com.quare.bibleplanner.core.installattribution.domain.model.InstallSource
import com.quare.bibleplanner.core.installattribution.domain.repository.InstallAttributionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReportAppInstallUseCaseTest {
    private val androidSource = InstallSource(
        platform = "android",
        referrer = "oppref=click-1&utm_source=openai",
        advertisingId = "gaid-1",
    )
    private lateinit var useCase: ReportAppInstallUseCase
    private lateinit var repository: FakeInstallAttributionRepository
    private var sourceReads = 0

    @Test
    fun `GIVEN an unreported install WHEN reporting THEN sends the click id and advertising id`() = runTest {
        // Given
        prepareScenario()

        // When
        useCase()

        // Then
        assertEquals(
            expected = listOf(
                InstallReport(
                    installId = INSTALL_ID,
                    platform = "android",
                    oppref = "click-1",
                    advertisingId = "gaid-1",
                ),
            ),
            actual = repository.sentReports,
        )
        assertTrue(repository.reported)
    }

    @Test
    fun `GIVEN an organic install WHEN reporting THEN still sends it without a click id`() = runTest {
        // Given
        prepareScenario(source = androidSource.copy(referrer = "utm_source=google-play&utm_medium=organic"))

        // When
        useCase()

        // Then
        assertEquals(
            expected = null,
            actual = repository.sentReports.single().oppref,
        )
        assertTrue(repository.reported)
    }

    @Test
    fun `GIVEN an unavailable referrer service WHEN reporting THEN sends the install without a click id`() = runTest {
        // Given
        prepareScenario(source = androidSource.copy(referrer = null, advertisingId = null))

        // When
        useCase()

        // Then
        assertEquals(
            expected = InstallReport(
                installId = INSTALL_ID,
                platform = "android",
                oppref = null,
                advertisingId = null,
            ),
            actual = repository.sentReports.single(),
        )
    }

    @Test
    fun `GIVEN an already reported install WHEN reporting THEN neither reads the source nor sends`() = runTest {
        // Given
        prepareScenario(alreadyReported = true)

        // When
        useCase()

        // Then
        assertEquals(
            expected = 0,
            actual = sourceReads,
        )
        assertEquals(
            expected = emptyList(),
            actual = repository.sentReports,
        )
    }

    @Test
    fun `GIVEN a platform without install attribution WHEN reporting THEN sends nothing`() = runTest {
        // Given
        prepareScenario(source = null)

        // When
        useCase()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = repository.sentReports,
        )
        assertFalse(repository.reported)
    }

    @Test
    fun `GIVEN a delivered report WHEN reporting twice THEN sends it once`() = runTest {
        // Given
        prepareScenario()

        // When
        useCase()
        useCase()

        // Then
        assertEquals(
            expected = 1,
            actual = repository.sentReports.size,
        )
    }

    @Test
    fun `GIVEN a rejected report WHEN reporting THEN marks it reported so it is not retried`() = runTest {
        // Given
        prepareScenario(outcomes = listOf(InstallReportOutcome.REJECTED))

        // When
        useCase()
        useCase()

        // Then
        assertTrue(repository.reported)
        assertEquals(
            expected = 1,
            actual = repository.sentReports.size,
        )
    }

    @Test
    fun `GIVEN a failed report WHEN reporting again THEN retries with the same install id`() = runTest {
        // Given
        prepareScenario(outcomes = listOf(InstallReportOutcome.FAILED, InstallReportOutcome.DELIVERED))
        useCase()
        assertFalse(repository.reported)

        // When
        useCase()

        // Then
        assertEquals(
            expected = listOf(INSTALL_ID, INSTALL_ID),
            actual = repository.sentReports.map(InstallReport::installId),
        )
        assertTrue(repository.reported)
    }

    @Test
    fun `GIVEN a source that throws WHEN reporting THEN swallows the error and stays unreported`() = runTest {
        // Given
        prepareScenario(sourceError = IllegalStateException("boom"))

        // When
        useCase()

        // Then
        assertFalse(repository.reported)
        assertEquals(
            expected = emptyList(),
            actual = repository.sentReports,
        )
    }

    private fun prepareScenario(
        source: InstallSource? = androidSource,
        alreadyReported: Boolean = false,
        outcomes: List<InstallReportOutcome> = listOf(InstallReportOutcome.DELIVERED),
        sourceError: Exception? = null,
    ) {
        sourceReads = 0
        repository = FakeInstallAttributionRepository(
            reported = alreadyReported,
            outcomes = ArrayDeque(outcomes),
        )
        useCase = ReportAppInstallUseCase(
            repository = repository,
            readInstallSource = {
                sourceReads++
                if (sourceError != null) throw sourceError
                source
            },
            referrerParser = InstallReferrerParser(),
        )
    }

    private companion object {
        const val INSTALL_ID = "install-1"
    }
}

private class FakeInstallAttributionRepository(
    var reported: Boolean,
    private val outcomes: ArrayDeque<InstallReportOutcome>,
) : InstallAttributionRepository {
    val sentReports = mutableListOf<InstallReport>()

    override suspend fun isInstallReported(): Boolean = reported

    override suspend fun markInstallReported() {
        reported = true
    }

    override suspend fun getOrCreateInstallId(): String = "install-1"

    override suspend fun sendInstallReport(report: InstallReport): InstallReportOutcome {
        sentReports += report
        return outcomes.removeFirst()
    }
}
