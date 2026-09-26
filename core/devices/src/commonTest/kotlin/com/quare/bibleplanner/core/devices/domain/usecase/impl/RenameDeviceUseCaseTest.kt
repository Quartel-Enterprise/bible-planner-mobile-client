package com.quare.bibleplanner.core.devices.domain.usecase.impl

import com.quare.bibleplanner.core.devices.fake.FakeDevicesRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RenameDeviceUseCaseTest {
    private lateinit var useCase: RenameDeviceUseCase
    private lateinit var repository: FakeDevicesRepository

    @Test
    fun `GIVEN a working repository WHEN renaming THEN succeeds and renames the device`() = runTest {
        // Given
        prepareScenario(renameFailure = null)

        // When
        val result = useCase(
            deviceRowId = "row-1",
            name = "Office PC",
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(
            expected = listOf("row-1" to "Office PC"),
            actual = repository.renames,
        )
    }

    @Test
    fun `GIVEN a failing repository WHEN renaming THEN returns the failure`() = runTest {
        // Given
        prepareScenario(renameFailure = IllegalStateException("disk full"))

        // When
        val result = useCase(
            deviceRowId = "row-1",
            name = "Office PC",
        )

        // Then
        val error = result.exceptionOrNull()
        assertIs<IllegalStateException>(error)
        assertEquals(
            expected = "disk full",
            actual = error.message,
        )
    }

    private fun prepareScenario(renameFailure: Throwable?) {
        repository = FakeDevicesRepository(renameFailure = renameFailure)
        useCase = RenameDeviceUseCase(repository)
    }
}
