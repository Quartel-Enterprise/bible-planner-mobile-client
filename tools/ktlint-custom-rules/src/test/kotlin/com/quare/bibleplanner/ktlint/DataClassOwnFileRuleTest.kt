package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class DataClassOwnFileRuleTest {
    private val dataClassOwnFileRuleAssertThat = assertThatRule { DataClassOwnFileRule() }

    @Test
    fun `GIVEN two data classes grouped in one file WHEN linting THEN reports each of them`() {
        // Given
        val code =
            """
            data class DayStudy(val id: Long)

            data class DayStudyFact(val text: String)
            """.trimIndent()

        // When
        val linted = dataClassOwnFileRuleAssertThat(code)

        // Then
        linted.hasLintViolationsWithoutAutoCorrect(
            LintViolation(1, 12, buildViolationMessage("DayStudy", "DayStudyFact")),
            LintViolation(3, 12, buildViolationMessage("DayStudyFact", "DayStudy")),
        )
    }

    @Test
    fun `GIVEN an enum declared next to the class that uses it WHEN linting THEN reports both`() {
        // Given
        val code =
            """
            data class SettingsUiState(val theme: ThemeOption)

            enum class ThemeOption { LIGHT, DARK }
            """.trimIndent()

        // When
        val linted = dataClassOwnFileRuleAssertThat(code)

        // Then
        linted.hasLintViolationsWithoutAutoCorrect(
            LintViolation(1, 12, buildViolationMessage("SettingsUiState", "ThemeOption")),
            LintViolation(3, 12, buildViolationMessage("ThemeOption", "SettingsUiState")),
        )
    }

    @Test
    fun `GIVEN a private data class next to a regular class WHEN linting THEN reports the data class`() {
        // Given
        val code =
            """
            class RecordingLogWriter

            private data class LogEntry(val tag: String)
            """.trimIndent()

        // When
        val linted = dataClassOwnFileRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(3, 20, buildViolationMessage("LogEntry", "RecordingLogWriter"))
    }

    @Test
    fun `GIVEN a data class alone in its file next to top-level functions WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            data class BookDataModel(val id: String)

            fun BookDataModel.isEmpty(): Boolean = id.isEmpty()
            """.trimIndent()

        // When
        val linted = dataClassOwnFileRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN data classes nested in a sealed hierarchy WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            sealed interface LoginUiEvent {
                data class OnProviderClick(val provider: String) : LoginUiEvent

                data object OnDismiss : LoginUiEvent
            }
            """.trimIndent()

        // When
        val linted = dataClassOwnFileRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a data class nested in the class that owns it WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class ProfileUiStateFactory {
                private data class BibleRow(val name: String?)
            }
            """.trimIndent()

        // When
        val linted = dataClassOwnFileRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a file of regular classes WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class FakeClock

            class FakeLogger
            """.trimIndent()

        // When
        val linted = dataClassOwnFileRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }
}

private fun buildViolationMessage(
    name: String,
    neighbour: String,
): String = "'$name' shares its file with '$neighbour': every data class and enum lives in a file of its own, " +
    "named after it"
