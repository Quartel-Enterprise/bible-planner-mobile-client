package com.quare.bibleplanner.core.remoteconfig.domain.service.impl

import com.quare.bibleplanner.core.remoteconfig.domain.service.Cancellable
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class RemoteConfigServiceImplTest {
    private lateinit var service: RemoteConfigServiceImpl
    private lateinit var dataSource: FakeRemoteConfigDataSource

    @Test
    fun `GIVEN a remote boolean WHEN observing it THEN emits the remote value`() = runTest {
        // Given
        prepareScenario(mapOf(KEY to true))

        // When
        val values = collect(
            observe = {
                service.observeBoolean(
                    key = KEY,
                    defaultValue = false,
                )
            },
        )

        // Then
        assertEquals(listOf(true), values)
    }

    @Test
    fun `GIVEN no remote int WHEN observing it THEN emits the default`() = runTest {
        // Given
        prepareScenario(emptyMap())

        // When
        val values = collect(
            observe = {
                service.observeInt(
                    key = KEY,
                    defaultValue = 5,
                )
            },
        )

        // Then
        assertEquals(listOf(5), values)
    }

    @Test
    fun `GIVEN an observed string WHEN the remote config updates THEN emits the new value`() = runTest {
        // Given
        prepareScenario(mapOf(KEY to "old"))
        val values = collect(
            observe = {
                service.observeString(
                    key = KEY,
                    defaultValue = "",
                )
            },
        )

        // When
        dataSource.update(mapOf(KEY to "new"))
        runCurrent()

        // Then
        assertEquals(listOf("old", "new"), values)
    }

    @Test
    fun `GIVEN an observed value WHEN an update leaves it unchanged THEN does not emit it again`() = runTest {
        // Given
        prepareScenario(mapOf(KEY to "same"))
        val values = collect(
            observe = {
                service.observeString(
                    key = KEY,
                    defaultValue = "",
                )
            },
        )

        // When
        dataSource.update(mapOf(KEY to "same"))
        runCurrent()

        // Then
        assertEquals(listOf("same"), values)
    }

    @Test
    fun `GIVEN an observed value WHEN the collector stops THEN removes the update listener`() = runTest {
        // Given
        prepareScenario(emptyMap())
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            service
                .observeBoolean(
                    key = KEY,
                    defaultValue = false,
                ).collect {}
        }

        // When
        job.cancel()
        runCurrent()

        // Then
        assertTrue(dataSource.listeners.isEmpty())
        assertEquals(1, dataSource.cancelledListeners)
    }

    private fun <T> TestScope.collect(observe: () -> Flow<T>): List<T> {
        val values = mutableListOf<T>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { observe().collect(values::add) }
        return values
    }

    private fun prepareScenario(values: Map<String, Any>) {
        dataSource = FakeRemoteConfigDataSource(values)
        service = RemoteConfigServiceImpl(dataSource)
    }

    private companion object {
        const val KEY = "remote_key"
    }
}

private class FakeRemoteConfigDataSource(
    private var values: Map<String, Any>,
) : RemoteConfigDataSource {
    val listeners = mutableListOf<() -> Unit>()
    var cancelledListeners = 0
        private set

    fun update(newValues: Map<String, Any>) {
        values = newValues
        listeners.toList().forEach { listener -> listener() }
    }

    override suspend fun getBoolean(key: String): Boolean? = values[key] as? Boolean

    override suspend fun getInt(key: String): Int? = values[key] as? Int

    override suspend fun getString(key: String): String? = values[key] as? String

    override fun addConfigUpdateListener(onUpdate: () -> Unit): Cancellable {
        listeners += onUpdate
        return Cancellable {
            listeners -= onUpdate
            cancelledListeners++
        }
    }
}
