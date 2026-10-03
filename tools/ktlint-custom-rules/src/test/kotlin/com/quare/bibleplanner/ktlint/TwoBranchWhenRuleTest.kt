package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class TwoBranchWhenRuleTest {
    private val twoBranchWhenRuleAssertThat = assertThatRule { TwoBranchWhenRule() }

    @Test
    fun `flags a subjectless when with one condition and an else`() {
        val code =
            """
            fun toHeaderRes(shouldShowDonate: Boolean): Int = when {
                shouldShowDonate -> 1
                else -> 2
            }
            """.trimIndent()

        twoBranchWhenRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 51, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags a when with a subject, one branch and an else`() {
        val code =
            """
            fun toEvent(group: WeekGroup): Int = when (group) {
                WeekGroup.Completed -> 1
                else -> 2
            }
            """.trimIndent()

        twoBranchWhenRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 38, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags a branch with several conditions next to an else`() {
        val code =
            """
            fun toReason(code: Int): Int = when (code) {
                NO_FILL, MEDIATION_NO_FILL -> 1
                else -> 2
            }
            """.trimIndent()

        twoBranchWhenRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 32, VIOLATION_MESSAGE)
    }

    @Test
    fun `allows three or more branches`() {
        val code =
            """
            fun toLabel(code: Int): String = when (code) {
                0 -> "none"
                1 -> "one"
                else -> "many"
            }
            """.trimIndent()

        twoBranchWhenRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows an exhaustive two-case dispatch without else`() {
        val code =
            """
            fun toLabel(state: ToggleState): String = when (state) {
                ToggleState.On -> "on"
                ToggleState.Off -> "off"
            }
            """.trimIndent()

        twoBranchWhenRuleAssertThat(code).hasNoLintViolations()
    }

    private companion object {
        const val VIOLATION_MESSAGE =
            "A when with a single branch besides else is an if/else: keep when for three or more branches"
    }
}
