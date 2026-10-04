package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class PrepareScenarioLastMemberRuleTest {
    private val prepareScenarioLastMemberRuleAssertThat = assertThatRule { PrepareScenarioLastMemberRule() }

    @Test
    fun `GIVEN a helper after prepareScenario WHEN linting THEN reports the helper`() {
        // Given
        val code =
            """
            class RepositoryTest {
                private fun prepareScenario() {
                    repository = Repository()
                }

                private fun buildPlan(): Plan = Plan(days = emptyList())
            }
            """.trimIndent()

        // When
        val linted = prepareScenarioLastMemberRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(6, 17, buildViolationMessage("buildPlan"))
    }

    @Test
    fun `GIVEN a property and a nested fake after prepareScenario WHEN linting THEN reports both`() {
        // Given
        val code =
            """
            class RepositoryTest {
                private fun prepareScenario() {
                    repository = Repository(dao = FakeDao())
                }

                private val plan = Plan(days = emptyList())

                private class FakeDao : Dao
            }
            """.trimIndent()

        // When
        val linted = prepareScenarioLastMemberRuleAssertThat(code)

        // Then
        linted.hasLintViolationsWithoutAutoCorrect(
            LintViolation(6, 17, buildViolationMessage("plan")),
            LintViolation(8, 19, buildViolationMessage("FakeDao")),
        )
    }

    @Test
    fun `GIVEN an init block after prepareScenario WHEN linting THEN reports the init block`() {
        // Given
        val code =
            """
            class RepositoryTest {
                private fun prepareScenario() {
                    repository = Repository()
                }

                init {
                    seedDatabase()
                }
            }
            """.trimIndent()

        // When
        val linted = prepareScenarioLastMemberRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(6, 5, buildViolationMessage("init block"))
    }

    @Test
    fun `GIVEN prepareScenario overloads and a companion object at the end WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class RepositoryTest {
                private lateinit var repository: Repository

                private fun prepareScenario() {
                    prepareScenario(theme = Theme.LIGHT)
                }

                private fun prepareScenario(theme: Theme) {
                    repository = Repository(theme = theme)
                }

                private companion object {
                    const val PLAN_ID = 1
                }
            }

            private class FakeDao : Dao
            """.trimIndent()

        // When
        val linted = prepareScenarioLastMemberRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a class without prepareScenario WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class RepositoryTest {
                private fun buildPlan(): Plan = Plan(days = emptyList())

                private val plan = buildPlan()
            }
            """.trimIndent()

        // When
        val linted = prepareScenarioLastMemberRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    private fun buildViolationMessage(name: String): String =
        "'$name' comes after 'prepareScenario', which is the last member of the class (only the companion " +
            "object may follow it): move it above, or to the top level after the class"
}
