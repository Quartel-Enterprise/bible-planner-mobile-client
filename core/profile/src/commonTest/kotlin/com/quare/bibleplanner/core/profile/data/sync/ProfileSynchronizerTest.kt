package com.quare.bibleplanner.core.profile.data.sync

import com.quare.bibleplanner.core.profile.fake.FakeProfileDao
import com.quare.bibleplanner.core.profile.fake.RecordingSupabaseClient
import com.quare.bibleplanner.core.provider.room.entity.ProfileEntity
import com.quare.bibleplanner.core.provider.supabase.testing.FakeRealtime
import com.quare.bibleplanner.core.sync.domain.FetchedSnapshot
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import io.github.jan.supabase.storage.storage
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileSynchronizerTest {
    private val firstRetryDelay = 2.seconds
    private val longerThanEveryRetry = 5.minutes
    private val avatarBytes = byteArrayOf(1, 2, 3)
    private lateinit var synchronizer: ProfileSynchronizer
    private lateinit var dao: FakeProfileDao
    private lateinit var delegate: RecordingSynchronizer
    private lateinit var supabase: RecordingSupabaseClient
    private lateinit var userId: MutableStateFlow<String?>

    @Test
    fun `GIVEN a pending avatar WHEN pushing once THEN uploads it before pushing the rest`() = runTest {
        // Given
        prepareScenario(initialRows = listOf(pendingAvatar()))

        // When
        synchronizer.pushPendingOnce()

        // Then
        val row = dao.rows.value.single()
        assertEquals(
            expected = "$PUBLIC_URL?v=$UPDATED_AT",
            actual = row.avatarUrl,
        )
        assertNull(row.pendingAvatarBytes)
        assertEquals(
            expected = listOf("pushPendingOnce"),
            actual = delegate.calls,
        )
    }

    @Test
    fun `GIVEN a failing avatar upload WHEN pushing once THEN still pushes the rest`() = runTest {
        // Given
        prepareScenario(initialRows = listOf(pendingAvatar()))
        supabase.responseStatus = HttpStatusCode.InternalServerError

        // When
        synchronizer.pushPendingOnce()

        // Then
        assertTrue(
            dao.rows.value
                .single()
                .pendingAvatarBytes != null,
        )
        assertEquals(
            expected = listOf("pushPendingOnce"),
            actual = delegate.calls,
        )
    }

    @Test
    fun `GIVEN a pending avatar while online WHEN running the push loop THEN uploads it`() = runTest {
        // Given
        prepareScenario(initialRows = listOf(pendingAvatar()))

        // When
        runPushLoop()

        // Then
        assertEquals(
            expected = "$PUBLIC_URL?v=$UPDATED_AT",
            actual = dao.rows.value
                .single()
                .avatarUrl,
        )
        assertEquals(
            expected = listOf("runPushLoop"),
            actual = delegate.calls,
        )
    }

    @Test
    fun `GIVEN a pending avatar while offline WHEN running the push loop THEN waits for the network`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(pendingAvatar()),
            isOnline = false,
        )

        // When
        runPushLoop()

        // Then
        assertTrue(supabase.requests.isEmpty())
    }

    @Test
    fun `GIVEN a failed avatar upload WHEN the backoff passes THEN retries the upload`() = runTest {
        // Given
        prepareScenario(initialRows = listOf(pendingAvatar()))
        supabase.responseStatus = HttpStatusCode.InternalServerError
        runPushLoop()

        // When
        supabase.responseStatus = HttpStatusCode.OK
        advanceTimeBy(firstRetryDelay)
        runCurrent()

        // Then
        assertEquals(
            expected = 2,
            actual = supabase.requests.size,
        )
        assertNull(
            dao.rows.value
                .single()
                .pendingAvatarBytes,
        )
    }

    @Test
    fun `GIVEN an avatar upload that always fails WHEN running the push loop THEN gives up after five uploads`() =
        runTest {
            // Given
            prepareScenario(initialRows = listOf(pendingAvatar()))
            supabase.responseStatus = HttpStatusCode.InternalServerError

            // When
            runPushLoop()
            advanceTimeBy(longerThanEveryRetry)
            runCurrent()

            // Then
            assertEquals(
                expected = 5,
                actual = supabase.requests.size,
            )
        }

    @Test
    fun `GIVEN the avatar of another account WHEN pushing once THEN does not upload it`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(pendingAvatar()),
            authenticatedUserId = "someone-else",
        )

        // When
        synchronizer.pushPendingOnce()

        // Then
        assertTrue(supabase.requests.isEmpty())
    }

    @Test
    fun `GIVEN nobody signed in WHEN pushing once THEN does not upload the avatar`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(pendingAvatar()),
            authenticatedUserId = null,
        )

        // When
        synchronizer.pushPendingOnce()

        // Then
        assertTrue(supabase.requests.isEmpty())
    }

    @Test
    fun `GIVEN a profile synchronizer WHEN running the rest of the sync THEN delegates to the generic one`() = runTest {
        // Given
        prepareScenario()

        // When
        synchronizer.seed(now = 1L)
        synchronizer.observeRealtime()
        synchronizer.fetchSnapshot()
        synchronizer.clearLocal()

        // Then
        assertEquals(
            expected = listOf("seed", "observeRealtime", "fetchSnapshot", "clearLocal"),
            actual = delegate.calls,
        )
    }

    private fun TestScope.runPushLoop() {
        backgroundScope.launch { synchronizer.runPushLoop() }
        runCurrent()
    }

    private fun pendingAvatar(): ProfileEntity = ProfileEntity(
        id = USER_ID,
        displayName = null,
        avatarUrl = null,
        pendingAvatarBytes = avatarBytes,
        updatedAt = UPDATED_AT,
        displayNamePendingSync = false,
        avatarPendingSync = true,
    )

    private fun prepareScenario(
        initialRows: List<ProfileEntity> = emptyList(),
        authenticatedUserId: String? = USER_ID,
        isOnline: Boolean = true,
    ) {
        dao = FakeProfileDao(initialRows = initialRows)
        delegate = RecordingSynchronizer()
        supabase = RecordingSupabaseClient(
            responseBody = UPLOAD_RESPONSE,
            realtime = FakeRealtime(actions = emptyFlow()),
        )
        userId = MutableStateFlow(authenticatedUserId)
        synchronizer = ProfileSynchronizer(
            delegate = delegate,
            profileDao = dao,
            avatarRemoteStore = AvatarRemoteStore(supabase.client.storage.from("avatars")),
            networkConnectivityObserver = { flowOf(isOnline) },
            getAuthenticatedUserId = { userId.value },
        )
    }

    private companion object {
        const val USER_ID = "user-id"
        const val UPDATED_AT = 1_000L
        const val PUBLIC_URL = "https://project.supabase.co/storage/v1/object/public/avatars/user-id/avatar.jpg"
        const val UPLOAD_RESPONSE = """{"Id":"file-1","Key":"avatars/user-id/avatar.jpg"}"""
    }
}

private class RecordingSynchronizer : Synchronizer {
    val calls = mutableListOf<String>()

    override suspend fun seed(now: Long) {
        calls += "seed"
    }

    override suspend fun runPushLoop() {
        calls += "runPushLoop"
        awaitCancellation()
    }

    override suspend fun pushPendingOnce() {
        calls += "pushPendingOnce"
    }

    override suspend fun observeRealtime() {
        calls += "observeRealtime"
    }

    override suspend fun fetchSnapshot(): FetchedSnapshot {
        calls += "fetchSnapshot"
        return FetchedSnapshot {}
    }

    override suspend fun clearLocal() {
        calls += "clearLocal"
    }
}
