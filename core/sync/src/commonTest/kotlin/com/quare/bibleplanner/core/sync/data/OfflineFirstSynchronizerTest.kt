package com.quare.bibleplanner.core.sync.data

import com.quare.bibleplanner.core.sync.domain.SyncLocalStore
import com.quare.bibleplanner.core.sync.domain.SyncRemoteStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class OfflineFirstSynchronizerTest {
    private lateinit var local: FakeLocalStore
    private lateinit var remote: FakeRemoteStore

    @Test
    fun `GIVEN pending and online WHEN syncing THEN pushes mapped dtos and marks them synced`() = runTest {
        // Given
        val sync = prepareScenario(online = true)
        runPushLoop(sync)

        // When
        local.pending.value = listOf("GEN")
        runCurrent()

        // Then
        assertEquals(listOf(listOf("user-1:GEN")), remote.upsertCalls)
        assertEquals(listOf("GEN"), local.markSyncedCalls)
    }

    @Test
    fun `GIVEN pending and offline WHEN syncing THEN does not push`() = runTest {
        // Given
        val sync = prepareScenario(online = false)
        runPushLoop(sync)

        // When
        local.pending.value = listOf("GEN")
        runCurrent()

        // Then
        assertTrue(remote.upsertCalls.isEmpty())
    }

    @Test
    fun `GIVEN a remote change WHEN it arrives THEN it is applied locally`() = runTest {
        // Given
        val sync = prepareScenario(online = false)
        backgroundScope.launch { sync.observeRealtime() }
        runCurrent()

        // When
        remote.remoteChanges.emit("GEN")
        runCurrent()

        // Then
        assertEquals(listOf("GEN"), local.applyRemoteCalls)
    }

    @Test
    fun `GIVEN a remote snapshot WHEN pulling THEN every row is applied locally`() = runTest {
        // Given
        val sync = prepareScenario(online = true)
        remote.snapshot = listOf("GEN", "EXO")

        // When
        sync.pullSnapshot()

        // Then
        assertEquals(listOf("GEN", "EXO"), local.applyRemoteCalls)
    }

    @Test
    fun `GIVEN pending WHEN flushing once THEN pushes and marks synced`() = runTest {
        // Given
        val sync = prepareScenario(online = true)
        local.pending.value = listOf("GEN")

        // When
        sync.pushPendingOnce()

        // Then
        assertEquals(listOf(listOf("user-1:GEN")), remote.upsertCalls)
        assertEquals(listOf("GEN"), local.markSyncedCalls)
    }

    private fun TestScope.runPushLoop(sync: OfflineFirstSynchronizer<String, String>) {
        backgroundScope.launch { sync.runPushLoop() }
        runCurrent()
    }

    private fun prepareScenario(online: Boolean): OfflineFirstSynchronizer<String, String> {
        local = FakeLocalStore()
        remote = FakeRemoteStore()
        return OfflineFirstSynchronizer(
            localStore = local,
            remoteStore = remote,
            networkConnectivityObserver = { flowOf(online) },
            getAuthenticatedUserId = { "user-1" },
            logTag = "Test",
        )
    }
}

private class FakeLocalStore : SyncLocalStore<String, String> {
    val pending = MutableStateFlow<List<String>>(emptyList())
    val markSyncedCalls = mutableListOf<String>()
    val applyRemoteCalls = mutableListOf<String>()

    override fun pendingFlow(): Flow<List<String>> = pending

    override suspend fun getPending(): List<String> = pending.value

    override suspend fun markSynced(entity: String) {
        markSyncedCalls += entity
    }

    override suspend fun applyRemote(dto: String) {
        applyRemoteCalls += dto
    }

    override fun toDto(
        userId: String,
        entity: String,
    ): String = "$userId:$entity"

    override suspend fun clearLocal() = Unit
}

private class FakeRemoteStore : SyncRemoteStore<String> {
    val upsertCalls = mutableListOf<List<String>>()
    val remoteChanges = MutableSharedFlow<String>()
    var snapshot = emptyList<String>()

    override suspend fun upsert(dtos: List<String>) {
        upsertCalls += dtos
    }

    override suspend fun fetch(userId: String): List<String> = snapshot

    override fun observeRemote(userId: String): Flow<String> = remoteChanges
}
