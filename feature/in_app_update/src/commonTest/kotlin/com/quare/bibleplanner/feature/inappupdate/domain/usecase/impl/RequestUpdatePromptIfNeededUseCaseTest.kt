package com.quare.bibleplanner.feature.inappupdate.domain.usecase.impl

import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptSource
import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.feature.inappupdate.fake.FakeUpdatePromptPreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

internal class RequestUpdatePromptIfNeededUseCaseTest {
    private var shownAvailability: UpdateAvailability.Available? = null
    private var shownSource: String? = null
    private var checked = false

    @Test
    fun `GIVEN no previous prompt AND an available update WHEN requesting THEN shows it with the startup source`() =
        runTest {
            val useCase = prepareScenario(
                availability = UpdateAvailability.Available(versionName = "2.0.0"),
                lastPromptedAt = null,
            )

            useCase()

            assertEquals(UpdateAvailability.Available(versionName = "2.0.0"), shownAvailability)
            assertEquals(UpdatePromptSource.STARTUP, shownSource)
        }

    @Test
    fun `GIVEN no available update WHEN requesting THEN shows nothing`() = runTest {
        val useCase = prepareScenario(availability = UpdateAvailability.NotAvailable)

        useCase()

        assertNull(shownAvailability)
    }

    @Test
    fun `GIVEN the last prompt was less than an hour ago WHEN requesting THEN does not check again`() = runTest {
        val useCase = prepareScenario(
            availability = UpdateAvailability.Available(versionName = "2.0.0"),
            lastPromptedAt = NOW - 59.minutes.inWholeMilliseconds,
        )

        useCase()

        assertFalse(checked)
        assertNull(shownAvailability)
    }

    @Test
    fun `GIVEN the last prompt was over an hour ago WHEN requesting THEN shows the prompt again`() = runTest {
        val useCase = prepareScenario(
            availability = UpdateAvailability.Available(versionName = "2.0.0"),
            lastPromptedAt = NOW - 1.hours.inWholeMilliseconds,
        )

        useCase()

        assertEquals(UpdateAvailability.Available(versionName = "2.0.0"), shownAvailability)
    }

    private fun onShowUpdatePrompt(
        availability: UpdateAvailability.Available,
        source: String,
    ) {
        shownAvailability = availability
        shownSource = source
    }

    private fun prepareScenario(
        availability: UpdateAvailability,
        lastPromptedAt: Long? = null,
    ): RequestUpdatePromptIfNeededUseCase = RequestUpdatePromptIfNeededUseCase(
        checkForUpdate = {
            checked = true
            availability
        },
        updatePromptPreferences = FakeUpdatePromptPreferences(lastPromptedAt = lastPromptedAt),
        currentTimestampProvider = { NOW },
        showUpdatePrompt = ::onShowUpdatePrompt,
    )

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
