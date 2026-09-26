package com.quare.bibleplanner.core.provider.supabase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.supabase.fake.FakePreferencesDataStore
import com.quare.bibleplanner.core.provider.supabase.session.MonitoredSessionManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.BucketApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.koin.core.Koin
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.seconds

internal class SupabaseModuleTest {
    private val sessionStatusTimeout = 5.seconds
    private lateinit var client: SupabaseClient
    private lateinit var koin: Koin

    @BeforeTest
    fun setUp() {
        client = getSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY,
            googleWebClientId = GOOGLE_WEB_CLIENT_ID,
            monitoredSessionManager = MemorySessionManager(),
        )
        koin = koinApplication {
            modules(
                supabaseModule,
                module {
                    single<DataStore<Preferences>> { FakePreferencesDataStore(emptyPreferences()) }
                    single<CurrentTimestampProvider> { CurrentTimestampProvider { NOW } }
                    single<SupabaseClient> { client }
                },
            )
        }.koin
    }

    @AfterTest
    fun tearDown() = runTest {
        koin.close()
        client.close()
    }

    @Test
    fun `GIVEN the supabase module WHEN resolving the session manager THEN monitors the platform session storage`() {
        // When
        val sessionManager = koin.get<SessionManager>()

        // Then
        assertIs<MonitoredSessionManager>(sessionManager)
    }

    @Test
    fun `GIVEN the supabase module WHEN resolving the client plugins THEN exposes the client's own plugins`() {
        // When
        val auth = koin.get<Auth>()
        val realtime = koin.get<Realtime>()
        val functions = koin.get<Functions>()

        // Then
        assertSame(client.auth, auth)
        assertSame(client.realtime, realtime)
        assertSame(client.functions, functions)
    }

    @Test
    fun `GIVEN the supabase module WHEN resolving the storage buckets THEN points each one at its bucket`() {
        // When
        val contentBucket = koin.get<BucketApi>(named(CONTENT_BUCKET))
        val avatarsBucket = koin.get<BucketApi>(named(AVATARS_BUCKET))

        // Then
        assertEquals(CONTENT_BUCKET, contentBucket.bucketId)
        assertEquals(AVATARS_BUCKET, avatarsBucket.bucketId)
    }

    @Test
    fun `GIVEN no stored session WHEN observing the session status THEN settles on not authenticated`() = runTest {
        // Given
        val sessionStatus = koin.get<StateFlow<SessionStatus>>()

        // When
        val settled = withContext(Dispatchers.Default) {
            withTimeout(sessionStatusTimeout) {
                sessionStatus.first { status ->
                    status !is SessionStatus.Initializing
                }
            }
        }

        // Then
        assertIs<SessionStatus.NotAuthenticated>(settled)
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
        const val SUPABASE_URL = "https://project.supabase.co"
        const val SUPABASE_KEY = "anon-key"
        const val GOOGLE_WEB_CLIENT_ID = "google-client-id"
    }
}
