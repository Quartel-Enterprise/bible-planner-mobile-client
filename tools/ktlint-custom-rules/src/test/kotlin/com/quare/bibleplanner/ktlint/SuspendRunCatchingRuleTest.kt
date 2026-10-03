package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class SuspendRunCatchingRuleTest {
    private val suspendRunCatchingRuleAssertThat = assertThatRule { SuspendRunCatchingRule() }

    @Test
    fun `flags rethrowing cancellation before catching Exception`() {
        val code =
            """
            suspend fun load() {
                try {
                    fetch()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log(e)
                }
            }
            """.trimIndent()

        suspendRunCatchingRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(2, 5, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags rethrowing a qualified cancellation before catching Throwable`() {
        val code =
            """
            suspend fun load() {
                try {
                    fetch()
                } catch (cancellation: kotlinx.coroutines.CancellationException) {
                    throw cancellation
                } catch (throwable: Throwable) {
                    log(throwable)
                }
            }
            """.trimIndent()

        suspendRunCatchingRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(2, 5, VIOLATION_MESSAGE)
    }

    @Test
    fun `allows handling cancellation on its own`() {
        val code =
            """
            suspend fun download() {
                try {
                    fetch()
                } catch (_: CancellationException) {
                    showPaused()
                } catch (e: Exception) {
                    log(e)
                }
            }
            """.trimIndent()

        suspendRunCatchingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows catching a specific exception`() {
        val code =
            """
            suspend fun load() {
                try {
                    fetch()
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (e: IOException) {
                    log(e)
                }
            }
            """.trimIndent()

        suspendRunCatchingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a try without a cancellation clause`() {
        val code =
            """
            fun parse(text: String): Int? = try {
                text.toInt()
            } catch (e: NumberFormatException) {
                null
            }
            """.trimIndent()

        suspendRunCatchingRuleAssertThat(code).hasNoLintViolations()
    }

    private companion object {
        const val VIOLATION_MESSAGE =
            "Rethrowing CancellationException and catching everything else by hand is what suspendRunCatching does: " +
                "use it and handle the Result with onSuccess/onFailure"
    }
}
