package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class DtoSerialNameRuleTest {
    private val dtoSerialNameRuleAssertThat = assertThatRule { DtoSerialNameRule() }

    @Test
    fun `flags a field without a serial name`() {
        val code =
            """
            @Serializable
            data class BookDto(
                val name: String,
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 9, buildMissingSerialNameMessage("name"))
    }

    @Test
    fun `flags a var field without a serial name`() {
        val code =
            """
            @Serializable
            class PlanDto(
                var title: String,
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 9, buildMissingSerialNameMessage("title"))
    }

    @Test
    fun `flags only the field that misses its serial name`() {
        val code =
            """
            @Serializable
            data class BookDto(
                @SerialName("id")
                val id: Long,
                val name: String,
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(5, 9, buildMissingSerialNameMessage("name"))
    }

    @Test
    fun `flags a field with a default value`() {
        val code =
            """
            @Serializable
            data class BookDto(
                @SerialName("name")
                val name: String = "",
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 24, buildDefaultValueMessage("name"))
    }

    @Test
    fun `flags a nullable field that defaults to null`() {
        val code =
            """
            @Serializable
            data class BookDto(
                @SerialName("cover_url")
                val coverUrl: String? = null,
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 29, buildDefaultValueMessage("coverUrl"))
    }

    @Test
    fun `flags both problems on the same field`() {
        val code =
            """
            @Serializable
            data class BookDto(
                val name: String = "",
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code)
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(3, 9, buildMissingSerialNameMessage("name")),
                LintViolation(3, 24, buildDefaultValueMessage("name")),
            )
    }

    @Test
    fun `allows a dto whose fields all name their key and have no default`() {
        val code =
            """
            @Serializable
            data class BookDto(
                @SerialName("id")
                val id: Long,
                @SerialName("cover_url")
                val coverUrl: String?,
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a class that is not a dto`() {
        val code =
            """
            data class Book(
                val name: String = "",
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a class that only mentions dto in the middle of its name`() {
        val code =
            """
            class BookDtoMapper(
                val fallbackTitle: String = "",
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a plain constructor parameter because it is not a field`() {
        val code =
            """
            @Serializable
            class BookDto(
                name: String = "",
            ) {
                @SerialName("name")
                val name: String = name
            }
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a dto that is not serializable`() {
        val code =
            """
            data class ProfileDto(
                val displayName: String?,
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a dto without a primary constructor`() {
        val code =
            """
            @Serializable
            class EmptyDto
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildMissingSerialNameMessage(name: String): String =
    "DTO field '$name' must declare its JSON key with @SerialName"

private fun buildDefaultValueMessage(name: String): String =
    "DTO field '$name' must not have a default value; make the type nullable and let 'explicitNulls = false' " +
        "read an absent key as null"
