package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class UnitFunctionBlockBodyRuleTest {
    private val unitFunctionBlockBodyRuleAssertThat = assertThatRule { UnitFunctionBlockBodyRule() }

    @Test
    fun `flags an expression body delegating to a function from another class`() {
        val code =
            """
            class ReaderSettingsRepositoryImpl(private val dataStore: DataStore) {
                suspend fun setNoteIconEnabled(isEnabled: Boolean) = dataStore.write(
                    key = noteIconEnabledKey,
                    value = isEnabled,
                )
            }
            """.trimIndent()

        unitFunctionBlockBodyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(
            2,
            17,
            buildMissingReturnTypeMessage("setNoteIconEnabled"),
        )
    }

    @Test
    fun `flags an override assigned to Unit`() {
        val code =
            """
            class FakeRepository : Repository {
                override suspend fun setEnabled(isEnabled: Boolean) = Unit
            }
            """.trimIndent()

        unitFunctionBlockBodyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(
            2,
            26,
            buildMissingReturnTypeMessage("setEnabled"),
        )
    }

    @Test
    fun `flags an explicit Unit return type with an expression body`() {
        val code =
            """
            fun logEvent(name: String): Unit = analytics.log(name)
            """.trimIndent()

        unitFunctionBlockBodyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(
            1,
            5,
            "Function 'logEvent' returns 'Unit' but uses an expression body; open a block body ({ … }) " +
                "instead of assigning with '='",
        )
    }

    @Test
    fun `flags a fully qualified Unit return type with an expression body`() {
        val code =
            """
            fun logEvent(name: String): kotlin.Unit = analytics.log(name)
            """.trimIndent()

        unitFunctionBlockBodyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(
            1,
            5,
            "Function 'logEvent' returns 'Unit' but uses an expression body; open a block body ({ … }) " +
                "instead of assigning with '='",
        )
    }

    @Test
    fun `flags an expression body calling an error function of another receiver`() {
        val code =
            """
            fun report(message: String) = logger.error(message)
            """.trimIndent()

        unitFunctionBlockBodyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(
            1,
            5,
            buildMissingReturnTypeMessage("report"),
        )
    }

    @Test
    fun `flags a non-Unit expression body without a return type`() {
        val code =
            """
            fun buildGreeting(name: String) = "Hello, ${'$'}name"
            """.trimIndent()

        unitFunctionBlockBodyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(
            1,
            5,
            buildMissingReturnTypeMessage("buildGreeting"),
        )
    }

    @Test
    fun `allows an expression body with a non-Unit return type`() {
        val code =
            """
            fun buildGreeting(name: String): String = "Hello, ${'$'}name"
            """.trimIndent()

        unitFunctionBlockBodyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a block body without a return type`() {
        val code =
            """
            fun logEvent(name: String) {
                analytics.log(name)
            }
            """.trimIndent()

        unitFunctionBlockBodyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a test assigned to runTest`() {
        val code =
            """
            class RepositoryTest {
                @Test
                fun `GIVEN a theme WHEN saving THEN stores it`() = runTest {
                    repository.setTheme(Theme.DARK)
                }
            }
            """.trimIndent()

        unitFunctionBlockBodyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a setup or teardown assigned to runTest`() {
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

        unitFunctionBlockBodyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows an expression body that never returns`() {
        val code =
            """
            class FakeRepository : Repository {
                override suspend fun applySyncedTheme(theme: Theme) = error("unused")

                override fun observeTheme() = TODO()

                override fun reset() = throw UnsupportedOperationException()

                override fun clear() = kotlin.error("unused")
            }
            """.trimIndent()

        unitFunctionBlockBodyRuleAssertThat(code).hasNoLintViolations()
    }

    private fun buildMissingReturnTypeMessage(name: String): String =
        "Function '$name' uses an expression body without a return type; declare the return type, or " +
            "open a block body ({ … }) if it returns 'Unit'"
}
