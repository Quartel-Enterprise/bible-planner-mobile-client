package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class DataClassDefaultValueRuleTest {
    private val dataClassDefaultValueRuleAssertThat = assertThatRule { DataClassDefaultValueRule() }

    @Test
    fun `flags a data class property with a default value`() {
        val code =
            """
            data class ProfileUiState(
                val name: String,
                val isLoading: Boolean = false,
            )
            """.trimIndent()

        dataClassDefaultValueRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 9, buildViolationMessage("isLoading"))
    }

    @Test
    fun `flags every defaulted property, including those with annotations`() {
        val code =
            """
            data class ChapterEntity(
                @PrimaryKey(autoGenerate = true) val id: Long = 0,
                @ColumnInfo(defaultValue = "NULL") val readUpdatedAt: Long? = null,
            )
            """.trimIndent()

        dataClassDefaultValueRuleAssertThat(code)
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(2, 42, buildViolationMessage("id")),
                LintViolation(3, 44, buildViolationMessage("readUpdatedAt")),
            )
    }

    @Test
    fun `flags a data class nested in a sealed interface`() {
        val code =
            """
            sealed interface PaywallUiAction {
                data class ShowSnackbar(
                    val args: List<Any> = emptyList(),
                ) : PaywallUiAction
            }
            """.trimIndent()

        dataClassDefaultValueRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 13, buildViolationMessage("args"))
    }

    @Test
    fun `allows a data class whose properties are all passed explicitly`() {
        val code =
            """
            data class ProfileUiState(
                val name: String,
                val isLoading: Boolean,
            )
            """.trimIndent()

        dataClassDefaultValueRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows default values in a regular class`() {
        val code =
            """
            class DesktopNetworkConnectivityObserver(
                private val pollInterval: Duration = 3.seconds,
            )
            """.trimIndent()

        dataClassDefaultValueRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `leaves serializable DTOs to the dto-serial-name rule`() {
        val code =
            """
            @Serializable
            data class EndChapterDto(
                @SerialName("verse") val verse: Int? = null,
            )
            """.trimIndent()

        dataClassDefaultValueRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildViolationMessage(name: String): String =
    "Data class property '$name' must not have a default value: pass it explicitly wherever the class is built"
