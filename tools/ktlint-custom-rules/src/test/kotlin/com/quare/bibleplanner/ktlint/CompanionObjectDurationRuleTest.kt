package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class CompanionObjectDurationRuleTest {
    private val companionObjectDurationRuleAssertThat = assertThatRule { CompanionObjectDurationRule() }

    @Test
    fun `flags a public Duration built from a number`() {
        val code =
            """
            class DevicesSynchronizer {
                companion object {
                    val INITIAL_BACKOFF = 2.seconds
                }
            }
            """.trimIndent()

        companionObjectDurationRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 13, buildViolationMessage("INITIAL_BACKOFF"))
    }

    @Test
    fun `flags a property typed as Duration`() {
        val code =
            """
            class Poller {
                companion object {
                    val pollInterval: Duration = readInterval()
                }
            }
            """.trimIndent()

        companionObjectDurationRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 13, buildViolationMessage("pollInterval"))
    }

    @Test
    fun `flags a Duration from a factory`() {
        val code =
            """
            class Poller {
                companion object {
                    internal val timeout = Duration.parse("PT1M")
                    val idle = 5.toDuration(DurationUnit.SECONDS)
                }
            }
            """.trimIndent()

        companionObjectDurationRuleAssertThat(code)
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(3, 22, buildViolationMessage("timeout")),
                LintViolation(4, 13, buildViolationMessage("idle")),
            )
    }

    @Test
    fun `leaves private Durations to the companion-object-constants rule`() {
        val code =
            """
            class Poller {
                private companion object {
                    val pollInterval = 3.seconds
                }
            }
            """.trimIndent()

        companionObjectDurationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a Duration as a private val in the class body`() {
        val code =
            """
            class DevicesSynchronizer {
                private val initialBackoff: Duration = 2.seconds
            }
            """.trimIndent()

        companionObjectDurationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows other constants in a companion object`() {
        val code =
            """
            class RewardedAd {
                companion object {
                    const val LOG_TAG = "RewardedAd"
                    val Empty = RewardedAd()
                }
            }
            """.trimIndent()

        companionObjectDurationRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildViolationMessage(name: String): String =
    "Duration '$name' should be a private val in the class body, not a companion object member"
