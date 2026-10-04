package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class PrepareScenarioReturnsUnitRuleTest {
    private val prepareScenarioReturnsUnitRuleAssertThat = assertThatRule { PrepareScenarioReturnsUnitRule() }

    @Test
    fun `GIVEN a prepareScenario returning the system under test WHEN linting THEN reports it`() {
        // Given
        val code =
            """
            class RepositoryTest {
                private fun prepareScenario(theme: Theme): Repository {
                    return Repository(theme = theme)
                }
            }
            """.trimIndent()

        // When
        val linted = prepareScenarioReturnsUnitRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 17, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a prepareScenario with an expression body WHEN linting THEN reports it`() {
        // Given
        val code =
            """
            class RepositoryTest {
                private fun TestScope.prepareScenario(theme: Theme) = Repository(theme = theme)
            }
            """.trimIndent()

        // When
        val linted = prepareScenarioReturnsUnitRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 27, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a prepareScenario declaring Unit WHEN linting THEN reports it`() {
        // Given
        val code =
            """
            class RepositoryTest {
                private fun prepareScenario(theme: Theme): Unit {
                    repository = Repository(theme = theme)
                }
            }
            """.trimIndent()

        // When
        val linted = prepareScenarioReturnsUnitRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 17, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a prepareScenario assigning properties WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class RepositoryTest {
                private lateinit var repository: Repository

                private suspend fun TestScope.prepareScenario(theme: Theme) {
                    repository = Repository(theme = theme)
                }
            }
            """.trimIndent()

        // When
        val linted = prepareScenarioReturnsUnitRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN another factory returning a value WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class RepositoryTest {
                private fun buildRepository(theme: Theme): Repository = Repository(theme = theme)
            }
            """.trimIndent()

        // When
        val linted = prepareScenarioReturnsUnitRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    private companion object {
        const val VIOLATION_MESSAGE =
            "'prepareScenario' returns nothing: give it a block body without a return type, and assign what the " +
                "tests use to lateinit var properties of the class"
    }
}
