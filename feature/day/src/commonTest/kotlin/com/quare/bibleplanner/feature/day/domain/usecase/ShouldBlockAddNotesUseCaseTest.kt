package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.plan.domain.usecase.GetDaysWithNotesCountUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetMaxFreeNotesAmountUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import com.quare.bibleplanner.feature.day.fake.FakeDayRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ShouldBlockAddNotesUseCaseTest {
    private lateinit var useCase: ShouldBlockAddNotesUseCase

    @Test
    fun `GIVEN a free user at the notes limit WHEN checking THEN blocks new notes`() = runTest {
        // Given
        prepareScenario(
            isFreeUser = true,
            daysWithNotes = 3,
        )

        // When
        val shouldBlock = useCase()

        // Then
        assertTrue(shouldBlock)
    }

    @Test
    fun `GIVEN a free user under the notes limit WHEN checking THEN allows new notes`() = runTest {
        // Given
        prepareScenario(
            isFreeUser = true,
            daysWithNotes = 2,
        )

        // When
        val shouldBlock = useCase()

        // Then
        assertFalse(shouldBlock)
    }

    @Test
    fun `GIVEN a pro user over the notes limit WHEN checking THEN allows new notes`() = runTest {
        // Given
        prepareScenario(
            isFreeUser = false,
            daysWithNotes = 10,
        )

        // When
        val shouldBlock = useCase()

        // Then
        assertFalse(shouldBlock)
    }

    private fun prepareScenario(
        isFreeUser: Boolean,
        daysWithNotes: Int,
    ) {
        useCase = ShouldBlockAddNotesUseCase(
            getDaysWithNotesCount = GetDaysWithNotesCountUseCase(
                FakeDayRepository(
                    day = null,
                    daysWithNotesCount = daysWithNotes,
                ),
            ),
            getMaxFreeNotesAmount = GetMaxFreeNotesAmountUseCase(FixedIntRemoteConfig(value = 3)),
            isFreeUser = { isFreeUser },
        )
    }
}

private class FixedIntRemoteConfig(
    private val value: Int,
) : GetIntRemoteConfig {
    override suspend fun invoke(
        key: String,
        default: Int,
    ): Int = value
}
