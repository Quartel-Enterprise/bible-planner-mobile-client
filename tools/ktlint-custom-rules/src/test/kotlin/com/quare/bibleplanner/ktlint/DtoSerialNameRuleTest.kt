package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class DtoSerialNameRuleTest {
    private val dtoSerialNameRuleAssertThat = assertThatRule { DtoSerialNameRule() }

    @Test
    fun `GIVEN a dto field without a serial name WHEN linting THEN reports the missing serial name`() {
        // Given
        val code =
            """
            @Serializable
            data class BookDto(
                val name: String,
            )
            """.trimIndent()

        // When
        val linted = dtoSerialNameRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(3, 9, buildMissingSerialNameMessage("name"))
    }

    @Test
    fun `GIVEN a dto var field without a serial name WHEN linting THEN reports the missing serial name`() {
        // Given
        val code =
            """
            @Serializable
            class PlanDto(
                var title: String,
            )
            """.trimIndent()

        // When
        val linted = dtoSerialNameRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(3, 9, buildMissingSerialNameMessage("title"))
    }

    @Test
    fun `GIVEN a dto with one field missing its serial name WHEN linting THEN reports only that field`() {
        // Given
        val code =
            """
            @Serializable
            data class BookDto(
                @SerialName("id")
                val id: Long,
                val name: String,
            )
            """.trimIndent()

        // When
        val linted = dtoSerialNameRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(5, 9, buildMissingSerialNameMessage("name"))
    }

    @Test
    fun `GIVEN a dto field with a default value WHEN linting THEN reports the default value`() {
        // Given
        val code =
            """
            @Serializable
            data class BookDto(
                @SerialName("name")
                val name: String = "",
            )
            """.trimIndent()

        // When
        val linted = dtoSerialNameRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(4, 24, buildDefaultValueMessage("name"))
    }

    @Test
    fun `GIVEN a nullable dto field that defaults to null WHEN linting THEN reports the default value`() {
        // Given
        val code =
            """
            @Serializable
            data class BookDto(
                @SerialName("cover_url")
                val coverUrl: String? = null,
            )
            """.trimIndent()

        // When
        val linted = dtoSerialNameRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(4, 29, buildDefaultValueMessage("coverUrl"))
    }

    @Test
    fun `GIVEN a dto field without a serial name and with a default WHEN linting THEN reports both problems`() {
        // Given
        val code =
            """
            @Serializable
            data class BookDto(
                val name: String = "",
            )
            """.trimIndent()

        // When
        val linted = dtoSerialNameRuleAssertThat(code)

        // Then
        linted.hasLintViolationsWithoutAutoCorrect(
            LintViolation(3, 9, buildMissingSerialNameMessage("name")),
            LintViolation(3, 24, buildDefaultValueMessage("name")),
        )
    }

    @Test
    fun `GIVEN a dto whose fields all name their key and have no default WHEN linting THEN reports nothing`() {
        // Given
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

        // When
        val linted = dtoSerialNameRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a class that is not a dto WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            data class Book(
                val name: String = "",
            )
            """.trimIndent()

        // When
        val linted = dtoSerialNameRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a class that only mentions dto in the middle of its name WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class BookDtoMapper(
                val fallbackTitle: String = "",
            )
            """.trimIndent()

        // When
        val linted = dtoSerialNameRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a dto with a plain constructor parameter WHEN linting THEN reports nothing`() {
        // Given
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

        // When
        val linted = dtoSerialNameRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a dto that is not serializable WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            data class ProfileDto(
                val displayName: String?,
            )
            """.trimIndent()

        // When
        val linted = dtoSerialNameRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a dto without a primary constructor WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            @Serializable
            class EmptyDto
            """.trimIndent()

        // When
        val linted = dtoSerialNameRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }
}

private fun buildMissingSerialNameMessage(name: String): String =
    "DTO field '$name' must declare its JSON key with @SerialName"

private fun buildDefaultValueMessage(name: String): String =
    "DTO field '$name' must not have a default value; make the type nullable and let 'explicitNulls = false' " +
        "read an absent key as null"
