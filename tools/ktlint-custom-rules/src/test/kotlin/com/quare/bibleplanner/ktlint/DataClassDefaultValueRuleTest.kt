package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class DataClassDefaultValueRuleTest {
    private val dataClassDefaultValueRuleAssertThat = assertThatRule { DataClassDefaultValueRule() }

    @Test
    fun `GIVEN a data class property with a default value WHEN linting THEN reports the property`() {
        // Given
        val code =
            """
            data class ProfileUiState(
                val name: String,
                val isLoading: Boolean = false,
            )
            """.trimIndent()

        // When
        val linted = dataClassDefaultValueRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(3, 9, buildViolationMessage("isLoading"))
    }

    @Test
    fun `GIVEN defaulted properties with annotations WHEN linting THEN reports every one of them`() {
        // Given
        val code =
            """
            data class ChapterEntity(
                @PrimaryKey(autoGenerate = true) val id: Long = 0,
                @ColumnInfo(defaultValue = "NULL") val readUpdatedAt: Long? = null,
            )
            """.trimIndent()

        // When
        val linted = dataClassDefaultValueRuleAssertThat(code)

        // Then
        linted.hasLintViolationsWithoutAutoCorrect(
            LintViolation(2, 42, buildViolationMessage("id")),
            LintViolation(3, 44, buildViolationMessage("readUpdatedAt")),
        )
    }

    @Test
    fun `GIVEN a defaulted data class nested in a sealed interface WHEN linting THEN reports the property`() {
        // Given
        val code =
            """
            sealed interface PaywallUiAction {
                data class ShowSnackbar(
                    val args: List<Any> = emptyList(),
                ) : PaywallUiAction
            }
            """.trimIndent()

        // When
        val linted = dataClassDefaultValueRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(3, 13, buildViolationMessage("args"))
    }

    @Test
    fun `GIVEN a data class whose properties are all passed explicitly WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            data class ProfileUiState(
                val name: String,
                val isLoading: Boolean,
            )
            """.trimIndent()

        // When
        val linted = dataClassDefaultValueRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN default values in a regular class WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class DesktopNetworkConnectivityObserver(
                private val pollInterval: Duration = 3.seconds,
            )
            """.trimIndent()

        // When
        val linted = dataClassDefaultValueRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a serializable DTO with a default value WHEN linting THEN leaves it to the dto-serial-name rule`() {
        // Given
        val code =
            """
            @Serializable
            data class EndChapterDto(
                @SerialName("verse") val verse: Int? = null,
            )
            """.trimIndent()

        // When
        val linted = dataClassDefaultValueRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }
}

private fun buildViolationMessage(name: String): String =
    "Data class property '$name' must not have a default value: pass it explicitly wherever the class is built"
