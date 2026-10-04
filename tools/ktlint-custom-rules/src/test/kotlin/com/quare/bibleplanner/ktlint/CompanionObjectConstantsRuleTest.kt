package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class CompanionObjectConstantsRuleTest {
    private val companionObjectConstantsRuleAssertThat = assertThatRule { CompanionObjectConstantsRule() }

    @Test
    fun `GIVEN a val inside a private companion object WHEN linting THEN reports the val`() {
        // Given
        val code =
            """
            class SplashViewModel {
                private companion object {
                    val defaultHoldDuration = 700.milliseconds
                }
            }
            """.trimIndent()

        // When
        val linted = companionObjectConstantsRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(3, 13, buildViolationMessage("defaultHoldDuration"))
    }

    @Test
    fun `GIVEN a private val inside a public companion object WHEN linting THEN reports the val`() {
        // Given
        val code =
            """
            class Cache {
                companion object {
                    private val entries = mutableMapOf<String, String>()
                }
            }
            """.trimIndent()

        // When
        val linted = companionObjectConstantsRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(3, 21, buildViolationMessage("entries"))
    }

    @Test
    fun `GIVEN a const val in a private companion object WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class KtorITunesRemoteDataSource {
                private companion object {
                    const val SEARCH_PATH = "search"
                }
            }
            """.trimIndent()

        // When
        val linted = companionObjectConstantsRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a public val that is part of the API of the type WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            data class PlaybackState(val position: Int) {
                companion object {
                    val Idle: PlaybackState = PlaybackState(position = 0)
                }
            }
            """.trimIndent()

        // When
        val linted = companionObjectConstantsRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a private val in the class body WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class QueueViewModel {
                private val emptyUiState = QueueUiState()
            }
            """.trimIndent()

        // When
        val linted = companionObjectConstantsRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a private val in a plain nested object WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class Holder {
                private object Defaults {
                    private val padding = 8
                }
            }
            """.trimIndent()

        // When
        val linted = companionObjectConstantsRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }
}

private fun buildViolationMessage(name: String): String =
    "Private val '$name' should be declared in the class body: a companion object holds constants and " +
        "public API, not the class's own private values"
