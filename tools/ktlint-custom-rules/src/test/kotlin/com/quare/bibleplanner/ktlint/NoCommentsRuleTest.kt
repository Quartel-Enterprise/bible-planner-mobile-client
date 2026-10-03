package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class NoCommentsRuleTest {
    private val noCommentsRuleAssertThat = assertThatRule { NoCommentsRule() }

    @Test
    fun `flags an end-of-line comment`() {
        val code =
            """
            fun load() {
                // fetch the books first
                fetchBooks()
            }
            """.trimIndent()

        noCommentsRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(2, 5, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags a trailing comment`() {
        val code =
            """
            val readTimestamp: Long? = null // epoch milliseconds
            """.trimIndent()

        noCommentsRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 33, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags a block comment`() {
        val code =
            """
            fun load() {
                /* fetch the books first */
                fetchBooks()
            }
            """.trimIndent()

        noCommentsRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(2, 5, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags a KDoc once, not each of its tags`() {
        val code =
            """
            /**
             * Loads the books.
             *
             * @return the books.
             */
            fun loadBooks(): List<Book> = emptyList()
            """.trimIndent()

        noCommentsRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 1, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags every comment in a file`() {
        val code =
            """
            // header
            class Books {
                /** The count. */
                val count = 0 // zero
            }
            """.trimIndent()

        noCommentsRuleAssertThat(code)
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(1, 1, VIOLATION_MESSAGE),
                LintViolation(3, 5, VIOLATION_MESSAGE),
                LintViolation(4, 19, VIOLATION_MESSAGE),
            )
    }

    @Test
    fun `allows comment-like text inside strings`() {
        val code =
            """
            val url = "https://example.com/*path*/"
            """.trimIndent()

        noCommentsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows code without comments`() {
        val code =
            """
            fun loadBooks(): List<Book> = emptyList()
            """.trimIndent()

        noCommentsRuleAssertThat(code).hasNoLintViolations()
    }

    private companion object {
        const val VIOLATION_MESSAGE = "Comments are not allowed in production code: let names say what the code means"
    }
}
