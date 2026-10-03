package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class CommentsSayWhyRuleTest {
    private val commentsSayWhyRuleAssertThat = assertThatRule { CommentsSayWhyRule() }

    @Test
    fun `flags an end-of-line comment that narrates the code`() {
        val code =
            """
            fun load() {
                // fetch the books first
                fetchBooks()
            }
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(2, 5, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags a trailing comment`() {
        val code =
            """
            val readTimestamp: Long? = null // epoch milliseconds
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 33, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags a block comment, even one that starts with Why`() {
        val code =
            """
            fun load() {
                /* Why: the books must be fetched first */
                fetchBooks()
            }
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(2, 5, VIOLATION_MESSAGE)
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

        commentsSayWhyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 1, VIOLATION_MESSAGE)
    }

    @Test
    fun `allows a Why comment`() {
        val code =
            """
            fun insets(): WindowInsets {
                // Why: CMP-10789 crashes on unattached insets when Nav3 moves a screen between scenes
                return WindowInsets.safeDrawing.asStable()
            }
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows the lines that continue a Why comment`() {
        val code =
            """
            data class ChapterEntity(
                // Why: read state syncs last-write-wins by readUpdatedAt, and the
                // pending flag is what the push loop picks up.
                val readUpdatedAt: Long?,
            )
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `flags a comment separated from a Why comment by a blank line`() {
        val code =
            """
            fun load() {
                // Why: the cache is warmed by the splash screen

                // fetch the books
                fetchBooks()
            }
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(4, 5, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags every disallowed comment in a file`() {
        val code =
            """
            // header
            class Books {
                /** The count. */
                val count = 0 // zero
            }
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code)
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

        commentsSayWhyRuleAssertThat(code).hasNoLintViolations()
    }

    private companion object {
        const val VIOLATION_MESSAGE =
            "Production code only keeps '// Why:' comments, which say why the code is the way it is: " +
                "say what it does with names instead"
    }
}
