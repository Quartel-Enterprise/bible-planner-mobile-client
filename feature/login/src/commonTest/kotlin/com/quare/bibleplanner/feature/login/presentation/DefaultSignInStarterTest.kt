package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import io.github.jan.supabase.compose.auth.composable.NativeSignInState
import io.github.jan.supabase.compose.auth.composable.NativeSignInStatus
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class DefaultSignInStarterTest {
    private lateinit var starter: DefaultSignInStarter
    private lateinit var nativeSignInState: NativeSignInState

    @BeforeTest
    fun setUp() {
        starter = DefaultSignInStarter()
        nativeSignInState = NativeSignInState(KotlinXSerializer())
    }

    @Test
    fun `GIVEN a native sign in state WHEN starting the sign in THEN starts the native flow`() = runTest {
        // When
        val result = starter(
            provider = LoginProvider.GOOGLE,
            nativeSignInState = nativeSignInState,
        )

        // Then
        assertTrue(result.isSuccess)
        assertIs<NativeSignInStatus.Started>(nativeSignInState.status)
    }
}
