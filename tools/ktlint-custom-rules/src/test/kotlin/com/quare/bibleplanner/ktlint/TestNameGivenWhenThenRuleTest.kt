package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class TestNameGivenWhenThenRuleTest {
    private val testNameGivenWhenThenRuleAssertThat = assertThatRule { TestNameGivenWhenThenRule() }

    @Test
    fun `GIVEN a test named in prose WHEN linting THEN reports the name`() {
        // Given
        val code =
            """
            class RepositoryTest {
                @Test
                fun `saves the theme`() {
                    repository.setTheme(Theme.DARK)
                }
            }
            """.trimIndent()

        // When
        val linted = testNameGivenWhenThenRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            3,
            9,
            "Test 'saves the theme' is not named 'GIVEN <state> WHEN <action> THEN <outcome>'",
        )
    }

    @Test
    fun `GIVEN a test with lowercase keywords WHEN linting THEN reports the name`() {
        // Given
        val code =
            """
            class RepositoryTest {
                @Test
                fun `given a theme when saving then stores it`() {
                    repository.setTheme(Theme.DARK)
                }
            }
            """.trimIndent()

        // When
        val linted = testNameGivenWhenThenRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            3,
            9,
            "Test 'given a theme when saving then stores it' is not named 'GIVEN <state> WHEN <action> THEN " +
                "<outcome>'",
        )
    }

    @Test
    fun `GIVEN a test without a THEN WHEN linting THEN reports the name`() {
        // Given
        val code =
            """
            class RepositoryTest {
                @Test
                fun `GIVEN a theme WHEN saving`() {
                    repository.setTheme(Theme.DARK)
                }
            }
            """.trimIndent()

        // When
        val linted = testNameGivenWhenThenRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            3,
            9,
            "Test 'GIVEN a theme WHEN saving' is not named 'GIVEN <state> WHEN <action> THEN <outcome>'",
        )
    }

    @Test
    fun `GIVEN a camelCase test WHEN linting THEN reports the name`() {
        // Given
        val code =
            """
            class RepositoryTest {
                @Test
                fun savesTheTheme() {
                    repository.setTheme(Theme.DARK)
                }
            }
            """.trimIndent()

        // When
        val linted = testNameGivenWhenThenRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            3,
            9,
            "Test 'savesTheTheme' is not named 'GIVEN <state> WHEN <action> THEN <outcome>'",
        )
    }

    @Test
    fun `GIVEN a name with an apostrophe WHEN linting THEN reports the character D8 rejects`() {
        // Given
        val code =
            """
            class RepositoryTest {
                @Test
                fun `GIVEN today's plan WHEN saving THEN stores it`() {
                    repository.save(plan)
                }
            }
            """.trimIndent()

        // When
        val linted = testNameGivenWhenThenRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            3,
            9,
            "Test 'GIVEN today's plan WHEN saving THEN stores it' may only hold letters, digits, spaces, '-' and " +
                "'_': D8 rejects any other character when the test is dexed for a device",
        )
    }

    @Test
    fun `GIVEN a name with a comma WHEN linting THEN reports the character D8 rejects`() {
        // Given
        val code =
            """
            class RepositoryTest {
                @Test
                fun `GIVEN a plan WHEN saving THEN stores it, then syncs`() {
                    repository.save(plan)
                }
            }
            """.trimIndent()

        // When
        val linted = testNameGivenWhenThenRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            3,
            9,
            "Test 'GIVEN a plan WHEN saving THEN stores it, then syncs' may only hold letters, digits, spaces, " +
                "'-' and '_': D8 rejects any other character when the test is dexed for a device",
        )
    }

    @Test
    fun `GIVEN a test named GIVEN WHEN THEN WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class RepositoryTest {
                @Test
                fun `GIVEN a day-1 plan WHEN saving it_now THEN stores 2 chapters`() = runTest {
                    repository.save(plan)
                }
            }
            """.trimIndent()

        // When
        val linted = testNameGivenWhenThenRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a function that is not a test WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class RepositoryTest {
                @BeforeTest
                fun setUp() {
                    repository.seed()
                }

                private fun buildPlan(): Plan = Plan(days = emptyList())
            }
            """.trimIndent()

        // When
        val linted = testNameGivenWhenThenRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }
}
