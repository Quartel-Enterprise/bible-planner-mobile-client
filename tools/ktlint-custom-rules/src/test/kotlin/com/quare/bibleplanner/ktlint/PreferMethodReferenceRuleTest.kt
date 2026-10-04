package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class PreferMethodReferenceRuleTest {
    private val preferMethodReferenceRuleAssertThat = assertThatRule { PreferMethodReferenceRule() }

    @Test
    fun `GIVEN a lambda forwarding its parameter to a same-file top level function WHEN linting THEN reports it`() {
        // Given
        val code =
            """
            fun mapDay(day: String): String = day

            fun mapAll(days: List<String>) = days.map { day -> mapDay(day) }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(3, 43, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a lambda forwarding the implicit parameter WHEN linting THEN reports it`() {
        // Given
        val code =
            """
            fun mapDay(day: String): String = day

            fun mapAll(days: List<String>) = days.map { mapDay(it) }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(3, 43, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a lambda forwarding to a member function of its own class WHEN linting THEN reports it`() {
        // Given
        val code =
            """
            class Mapper {
                fun mapAll(days: List<String>) = days.map { day -> mapDay(day) }

                private fun mapDay(day: String): String = day
            }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 47, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a lambda forwarding to a suspend function WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            suspend fun mapDay(day: String): String = day

            suspend fun mapAll(days: List<String>) = days.map { day -> mapDay(day) }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a lambda forwarding to a composable function WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            @Composable
            fun DayRow(day: String) = Unit

            fun render(days: List<String>) = days.forEach { day -> DayRow(day) }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a forwarding lambda inside a composable function WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun mapDay(day: String): String = day

            @Composable
            fun DayList(days: List<String>) {
                days.map { day -> mapDay(day) }
            }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a lambda forwarding to a function declared in another file WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun mapAll(days: List<String>) = days.map { day -> mapDay(day) }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a lambda forwarding to a call reached through a receiver WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun delete(id: String) = Unit

            class Cleaner(private val repository: Repository) {
                fun clean(id: String?) = id?.let { repository.delete(it) }
            }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a lambda forwarding to a member function of another class WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class Mapper {
                fun mapDay(day: String): String = day
            }

            class Caller {
                fun mapAll(days: List<String>, mapper: Mapper) = days.map { day -> mapper.mapDay(day) }
            }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a lambda taking more than one parameter WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun mapDay(day: String): String = day

            fun mapAll(days: Map<String, String>) = days.map { key, _ -> mapDay(key) }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a lambda destructuring its parameter WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun mapDay(day: String): String = day

            fun mapAll(days: List<Pair<String, String>>) = days.map { (day, _) -> mapDay(day) }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a lambda whose call adds arguments of its own WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun mapDay(day: String, uppercase: Boolean): String = day

            fun mapAll(days: List<String>) = days.map { day -> mapDay(day, true) }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a lambda whose call names its argument WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun mapDay(day: String): String = day

            fun mapAll(days: List<String>) = days.map { day -> mapDay(day = day) }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a lambda whose call carries type arguments WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun <T> mapDay(day: T): T = day

            fun mapAll(days: List<String>) = days.map { day -> mapDay<String>(day) }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a lambda that does more than forward WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun mapDay(day: String): String = day

            fun mapAll(days: List<String>) = days.map { day ->
                println(day)
                mapDay(day)
            }
            """.trimIndent()

        // When
        val linted = preferMethodReferenceRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    private companion object {
        const val VIOLATION_MESSAGE =
            "Lambda only forwards its parameter to 'mapDay' — pass a method reference (::mapDay) instead of " +
                "wrapping the call in a lambda"
    }
}
