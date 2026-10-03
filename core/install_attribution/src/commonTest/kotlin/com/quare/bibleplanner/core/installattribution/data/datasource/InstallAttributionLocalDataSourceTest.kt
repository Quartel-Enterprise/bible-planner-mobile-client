package com.quare.bibleplanner.core.installattribution.data.datasource

import androidx.datastore.preferences.core.emptyPreferences
import com.quare.bibleplanner.core.provider.datastore.testing.FakePreferencesDataStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InstallAttributionLocalDataSourceTest {
    private lateinit var dataSource: InstallAttributionLocalDataSource

    @Test
    fun `GIVEN a fresh install WHEN reading the reported flag THEN is not reported`() = runTest {
        // Given
        prepareScenario()

        // When
        val reported = dataSource.isReported()

        // Then
        assertFalse(reported)
    }

    @Test
    fun `GIVEN a fresh install WHEN marking it reported THEN is reported`() = runTest {
        // Given
        prepareScenario()

        // When
        dataSource.markReported()

        // Then
        assertTrue(dataSource.isReported())
    }

    @Test
    fun `GIVEN no install id WHEN reading it twice THEN creates one id and keeps it`() = runTest {
        // Given
        prepareScenario()

        // When
        val first = dataSource.getOrCreateInstallId()
        val second = dataSource.getOrCreateInstallId()

        // Then
        assertTrue(first.isNotBlank())
        assertEquals(
            expected = first,
            actual = second,
        )
    }

    private fun prepareScenario() {
        dataSource = InstallAttributionLocalDataSource(FakePreferencesDataStore(emptyPreferences()))
    }
}
