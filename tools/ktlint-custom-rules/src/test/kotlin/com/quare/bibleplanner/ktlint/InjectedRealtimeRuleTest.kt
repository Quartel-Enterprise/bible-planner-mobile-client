package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class InjectedRealtimeRuleTest {
    private val injectedRealtimeRuleAssertThat = assertThatRule { InjectedRealtimeRule() }

    @Test
    fun `GIVEN the client realtime extension imported WHEN linting THEN reports the import`() {
        // Given
        val code =
            """
            import io.github.jan.supabase.SupabaseClient
            import io.github.jan.supabase.realtime.realtime

            class Store(private val supabaseClient: SupabaseClient) {
                private val realtime get() = supabaseClient.realtime
            }
            """.trimIndent()

        // When
        val linted = injectedRealtimeRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 1, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN the realtime plugin looked up on the client WHEN linting THEN reports the lookup`() {
        // Given
        val code =
            """
            class Store(private val supabaseClient: SupabaseClient) {
                private val realtime = supabaseClient.pluginManager.getPlugin(Realtime)
            }
            """.trimIndent()

        // When
        val linted = injectedRealtimeRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 57, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN the injected Realtime WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            import io.github.jan.supabase.realtime.Realtime
            import io.github.jan.supabase.realtime.channel

            class Store(private val realtime: Realtime) {
                fun observe() = realtime.channel("favorites")
            }
            """.trimIndent()

        // When
        val linted = injectedRealtimeRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    private companion object {
        const val VIOLATION_MESSAGE =
            "SupabaseClient.realtime is the raw plugin, which removes channels without the serialization " +
                "SerializedRemovalRealtime adds: inject Realtime instead"
    }
}
