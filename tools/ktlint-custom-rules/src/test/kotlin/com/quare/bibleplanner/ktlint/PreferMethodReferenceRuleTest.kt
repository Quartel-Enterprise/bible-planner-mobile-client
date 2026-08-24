package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class PreferMethodReferenceRuleTest {
    private val preferMethodReferenceRuleAssertThat = assertThatRule { PreferMethodReferenceRule() }

    @Test
    fun `flags a lambda forwarding its parameter to a top level function in the same file`() {
        val code =
            """
            fun mapDay(day: String): String = day

            fun mapAll(days: List<String>) = days.map { day -> mapDay(day) }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 43, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags a lambda forwarding the implicit parameter`() {
        val code =
            """
            fun mapDay(day: String): String = day

            fun mapAll(days: List<String>) = days.map { mapDay(it) }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 43, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags a lambda forwarding to a member function of the class it sits in`() {
        val code =
            """
            class Mapper {
                fun mapAll(days: List<String>) = days.map { day -> mapDay(day) }

                private fun mapDay(day: String): String = day
            }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 47, VIOLATION_MESSAGE)
    }

    @Test
    fun `does not flag a suspend function, whose reference does not fit a plain function type`() {
        val code =
            """
            suspend fun mapDay(day: String): String = day

            suspend fun mapAll(days: List<String>) = days.map { day -> mapDay(day) }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag a composable function, which cannot be referenced`() {
        val code =
            """
            @Composable
            fun DayRow(day: String) = Unit

            fun render(days: List<String>) = days.forEach { day -> DayRow(day) }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag a lambda inside a composable function`() {
        val code =
            """
            fun mapDay(day: String): String = day

            @Composable
            fun DayList(days: List<String>) {
                days.map { day -> mapDay(day) }
            }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag a function it cannot see, declared in another file`() {
        val code =
            """
            fun mapAll(days: List<String>) = days.map { day -> mapDay(day) }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag a call reached through a receiver`() {
        val code =
            """
            fun delete(id: String) = Unit

            class Cleaner(private val repository: Repository) {
                fun clean(id: String?) = id?.let { repository.delete(it) }
            }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag a member function of a different class in the same file`() {
        val code =
            """
            class Mapper {
                fun mapDay(day: String): String = day
            }

            class Caller {
                fun mapAll(days: List<String>, mapper: Mapper) = days.map { day -> mapper.mapDay(day) }
            }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag a lambda taking more than one parameter`() {
        val code =
            """
            fun mapDay(day: String): String = day

            fun mapAll(days: Map<String, String>) = days.map { key, _ -> mapDay(key) }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag a lambda destructuring its parameter`() {
        val code =
            """
            fun mapDay(day: String): String = day

            fun mapAll(days: List<Pair<String, String>>) = days.map { (day, _) -> mapDay(day) }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag a call that adds arguments of its own`() {
        val code =
            """
            fun mapDay(day: String, uppercase: Boolean): String = day

            fun mapAll(days: List<String>) = days.map { day -> mapDay(day, true) }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag a call naming its argument, which a reference cannot carry`() {
        val code =
            """
            fun mapDay(day: String): String = day

            fun mapAll(days: List<String>) = days.map { day -> mapDay(day = day) }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag a call carrying type arguments`() {
        val code =
            """
            fun <T> mapDay(day: T): T = day

            fun mapAll(days: List<String>) = days.map { day -> mapDay<String>(day) }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag a lambda that does more than forward`() {
        val code =
            """
            fun mapDay(day: String): String = day

            fun mapAll(days: List<String>) = days.map { day ->
                println(day)
                mapDay(day)
            }
            """.trimIndent()

        preferMethodReferenceRuleAssertThat(code).hasNoLintViolations()
    }

    private companion object {
        const val VIOLATION_MESSAGE =
            "Lambda only forwards its parameter to 'mapDay' — pass a method reference (::mapDay) instead of " +
                "wrapping the call in a lambda"
    }
}
