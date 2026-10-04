package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class UnitFunctionBlockBodyRuleTest {
    private val unitFunctionBlockBodyRuleAssertThat = assertThatRule { UnitFunctionBlockBodyRule() }

    @Test
    fun `GIVEN an expression body delegating to another class WHEN linting THEN reports the missing return type`() {
        // Given
        val code =
            """
            class ReaderSettingsRepositoryImpl(private val dataStore: DataStore) {
                suspend fun setNoteIconEnabled(isEnabled: Boolean) = dataStore.write(
                    key = noteIconEnabledKey,
                    value = isEnabled,
                )
            }
            """.trimIndent()

        // When
        val linted = unitFunctionBlockBodyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            2,
            17,
            buildMissingReturnTypeMessage("setNoteIconEnabled"),
        )
    }

    @Test
    fun `GIVEN an override assigned to Unit WHEN linting THEN reports the missing return type`() {
        // Given
        val code =
            """
            class FakeRepository : Repository {
                override suspend fun setEnabled(isEnabled: Boolean) = Unit
            }
            """.trimIndent()

        // When
        val linted = unitFunctionBlockBodyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            2,
            26,
            buildMissingReturnTypeMessage("setEnabled"),
        )
    }

    @Test
    fun `GIVEN an explicit Unit return type with an expression body WHEN linting THEN reports the expression body`() {
        // Given
        val code =
            """
            fun logEvent(name: String): Unit = analytics.log(name)
            """.trimIndent()

        // When
        val linted = unitFunctionBlockBodyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            1,
            5,
            "Function 'logEvent' returns 'Unit' but uses an expression body; open a block body ({ … }) " +
                "instead of assigning with '='",
        )
    }

    @Test
    fun `GIVEN a qualified Unit return type with an expression body WHEN linting THEN reports the expression body`() {
        // Given
        val code =
            """
            fun logEvent(name: String): kotlin.Unit = analytics.log(name)
            """.trimIndent()

        // When
        val linted = unitFunctionBlockBodyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            1,
            5,
            "Function 'logEvent' returns 'Unit' but uses an expression body; open a block body ({ … }) " +
                "instead of assigning with '='",
        )
    }

    @Test
    fun `GIVEN an expression body calling logger error WHEN linting THEN reports the missing return type`() {
        // Given
        val code =
            """
            fun report(message: String) = logger.error(message)
            """.trimIndent()

        // When
        val linted = unitFunctionBlockBodyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            1,
            5,
            buildMissingReturnTypeMessage("report"),
        )
    }

    @Test
    fun `GIVEN a non-Unit expression body without a return type WHEN linting THEN reports the missing return type`() {
        // Given
        val code =
            """
            fun buildGreeting(name: String) = "Hello, ${'$'}name"
            """.trimIndent()

        // When
        val linted = unitFunctionBlockBodyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            1,
            5,
            buildMissingReturnTypeMessage("buildGreeting"),
        )
    }

    @Test
    fun `GIVEN an expression body with a non-Unit return type WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun buildGreeting(name: String): String = "Hello, ${'$'}name"
            """.trimIndent()

        // When
        val linted = unitFunctionBlockBodyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a block body without a return type WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun logEvent(name: String) {
                analytics.log(name)
            }
            """.trimIndent()

        // When
        val linted = unitFunctionBlockBodyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a test assigned to runTest WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class RepositoryTest {
                @Test
                fun `GIVEN a theme WHEN saving THEN stores it`() = runTest {
                    repository.setTheme(Theme.DARK)
                }
            }
            """.trimIndent()

        // When
        val linted = unitFunctionBlockBodyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a setup and a teardown assigned to runTest WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class RepositoryTest {
                @BeforeTest
                fun setUp() = runTest(testDispatcher) {
                    repository.seed()
                }

                @AfterTest
                fun tearDown() = runTest {
                    database.close()
                }
            }
            """.trimIndent()

        // When
        val linted = unitFunctionBlockBodyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN expression bodies that never return WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class FakeRepository : Repository {
                override suspend fun applySyncedTheme(theme: Theme) = error("unused")

                override fun observeTheme() = TODO()

                override fun reset() = throw UnsupportedOperationException()

                override fun clear() = kotlin.error("unused")
            }
            """.trimIndent()

        // When
        val linted = unitFunctionBlockBodyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    private fun buildMissingReturnTypeMessage(name: String): String =
        "Function '$name' uses an expression body without a return type; declare the return type, or " +
            "open a block body ({ … }) if it returns 'Unit'"
}
