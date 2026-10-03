package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class DataClassOwnFileRuleTest {
    private val dataClassOwnFileRuleAssertThat = assertThatRule { DataClassOwnFileRule() }

    @Test
    fun `flags each data class grouped with another one`() {
        val code =
            """
            data class DayStudy(val id: Long)

            data class DayStudyFact(val text: String)
            """.trimIndent()

        dataClassOwnFileRuleAssertThat(code)
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(1, 12, buildViolationMessage("DayStudy", "DayStudyFact")),
                LintViolation(3, 12, buildViolationMessage("DayStudyFact", "DayStudy")),
            )
    }

    @Test
    fun `flags an enum declared next to the class that uses it`() {
        val code =
            """
            data class SettingsUiState(val theme: ThemeOption)

            enum class ThemeOption { LIGHT, DARK }
            """.trimIndent()

        dataClassOwnFileRuleAssertThat(code)
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(1, 12, buildViolationMessage("SettingsUiState", "ThemeOption")),
                LintViolation(3, 12, buildViolationMessage("ThemeOption", "SettingsUiState")),
            )
    }

    @Test
    fun `flags a private data class next to a regular class`() {
        val code =
            """
            class RecordingLogWriter

            private data class LogEntry(val tag: String)
            """.trimIndent()

        dataClassOwnFileRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 20, buildViolationMessage("LogEntry", "RecordingLogWriter"))
    }

    @Test
    fun `allows a data class alone in its file, next to top-level functions`() {
        val code =
            """
            data class BookDataModel(val id: String)

            fun BookDataModel.isEmpty(): Boolean = id.isEmpty()
            """.trimIndent()

        dataClassOwnFileRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows data classes nested in a sealed hierarchy`() {
        val code =
            """
            sealed interface LoginUiEvent {
                data class OnProviderClick(val provider: String) : LoginUiEvent

                data object OnDismiss : LoginUiEvent
            }
            """.trimIndent()

        dataClassOwnFileRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a data class nested in the class that owns it`() {
        val code =
            """
            class ProfileUiStateFactory {
                private data class BibleRow(val name: String?)
            }
            """.trimIndent()

        dataClassOwnFileRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a file of regular classes`() {
        val code =
            """
            class FakeClock

            class FakeLogger
            """.trimIndent()

        dataClassOwnFileRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildViolationMessage(
    name: String,
    neighbour: String,
): String = "'$name' shares its file with '$neighbour': every data class and enum lives in a file of its own, " +
    "named after it"
