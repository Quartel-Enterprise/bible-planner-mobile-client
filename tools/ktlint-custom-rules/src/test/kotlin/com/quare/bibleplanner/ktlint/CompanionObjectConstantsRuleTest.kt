package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class CompanionObjectConstantsRuleTest {
    private val companionObjectConstantsRuleAssertThat = assertThatRule { CompanionObjectConstantsRule() }

    @Test
    fun `flags a val inside a private companion object`() {
        val code =
            """
            class SplashViewModel {
                private companion object {
                    val defaultHoldDuration = 700.milliseconds
                }
            }
            """.trimIndent()

        companionObjectConstantsRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 13, buildViolationMessage("defaultHoldDuration"))
    }

    @Test
    fun `flags a private val inside a public companion object`() {
        val code =
            """
            class Cache {
                companion object {
                    private val entries = mutableMapOf<String, String>()
                }
            }
            """.trimIndent()

        companionObjectConstantsRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 21, buildViolationMessage("entries"))
    }

    @Test
    fun `allows a const val in a private companion object`() {
        val code =
            """
            class KtorITunesRemoteDataSource {
                private companion object {
                    const val SEARCH_PATH = "search"
                }
            }
            """.trimIndent()

        companionObjectConstantsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a public val that is part of the type's API`() {
        val code =
            """
            data class PlaybackState(val position: Int) {
                companion object {
                    val Idle: PlaybackState = PlaybackState(position = 0)
                }
            }
            """.trimIndent()

        companionObjectConstantsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private val in the class body`() {
        val code =
            """
            class QueueViewModel {
                private val emptyUiState = QueueUiState()
            }
            """.trimIndent()

        companionObjectConstantsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private val in a plain nested object`() {
        val code =
            """
            class Holder {
                private object Defaults {
                    private val padding = 8
                }
            }
            """.trimIndent()

        companionObjectConstantsRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildViolationMessage(name: String): String =
    "Private val '$name' should be declared in the class body: a companion object holds constants and " +
        "public API, not the class's own private values"
