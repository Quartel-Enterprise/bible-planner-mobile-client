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
    fun `flags a block comment that does not start with Why`() {
        val code =
            """
            fun load() {
                /*
                 * the books must be fetched first,
                 * then the plans
                 */
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
    fun `allows a multi-line Why block comment`() {
        val code =
            """
            data class ChapterEntity(
                /*
                 * Why: read state syncs last-write-wins by readUpdatedAt, and the
                 *
                 * pending flag is what the push loop picks up.
                 */
                val readUpdatedAt: Long?,
            )
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `turns a Why comment continued on line comments into a block comment`() {
        val code =
            """
            data class ChapterEntity(
                // Why: read state syncs last-write-wins by readUpdatedAt, and the
                //
                // pending flag is what the push loop picks up.
                val readUpdatedAt: Long?,
            )
            """.trimIndent()
        val formattedCode =
            """
            data class ChapterEntity(
                /*
                 * Why: read state syncs last-write-wins by readUpdatedAt, and the
                 *
                 * pending flag is what the push loop picks up.
                 */
                val readUpdatedAt: Long?,
            )
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code)
            .hasLintViolation(2, 5, MULTI_LINE_MESSAGE)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `turns a multi-line Why comment at the top of a file into a block comment`() {
        val code =
            """
            // Why: CMP-10888 drops the relayout requested in the frame
            // the shared transition ends.
            fun relayout() = Unit
            """.trimIndent()
        val formattedCode =
            """
            /*
             * Why: CMP-10888 drops the relayout requested in the frame
             * the shared transition ends.
             */
            fun relayout() = Unit
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code)
            .hasLintViolation(1, 1, MULTI_LINE_MESSAGE)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `keeps consecutive one-line Why comments apart`() {
        val code =
            """
            fun load() {
                // Why: the cache is warmed by the splash screen.
                // Why: the books must be fetched before the plans.
                fetchBooks()
            }
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `flags a multi-line trailing Why comment without autocorrecting it`() {
        val code =
            """
            fun load() {
                fetchBooks() // Why: the books must be fetched
                // before the plans.
            }
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(2, 18, MULTI_LINE_MESSAGE)
    }

    @Test
    fun `flags a multi-line Why comment holding comment delimiters without autocorrecting it`() {
        val code =
            """
            fun load() {
                // Why: the server matches paths like /books/*
                // so the wildcard stays in the URL.
                fetchBooks()
            }
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(2, 5, MULTI_LINE_MESSAGE)
    }

    @Test
    fun `turns a one-line Why block comment into a line comment`() {
        val code =
            """
            fun load() {
                /* Why: the books must be fetched first */
                fetchBooks()
            }
            """.trimIndent()
        val formattedCode =
            """
            fun load() {
                // Why: the books must be fetched first
                fetchBooks()
            }
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code)
            .hasLintViolation(2, 5, SINGLE_LINE_MESSAGE)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `flags a one-line Why block comment followed by code without autocorrecting it`() {
        val code =
            """
            fun load() {
                fetchBooks(/* Why: positional for the old API */ 1, 2)
            }
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(2, 16, SINGLE_LINE_MESSAGE)
    }

    @Test
    fun `keeps the inner indentation of a Why block comment written without stars`() {
        val code =
            """
            fun load() {
                /* Why: the books load in this order:
                     1. the downloaded versions
                       - the default one first
                     2. the remote ones */
                fetchBooks()
            }
            """.trimIndent()
        val formattedCode =
            """
            fun load() {
                /*
                 * Why: the books load in this order:
                 * 1. the downloaded versions
                 *   - the default one first
                 * 2. the remote ones
                 */
                fetchBooks()
            }
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code)
            .hasLintViolation(2, 5, MULTI_LINE_MESSAGE)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `adds the stars to a Why block comment written without them`() {
        val code =
            """
            fun load() {
                /* Why: the books must be fetched
                   before the plans. */
                fetchBooks()
            }
            """.trimIndent()
        val formattedCode =
            """
            fun load() {
                /*
                 * Why: the books must be fetched
                 * before the plans.
                 */
                fetchBooks()
            }
            """.trimIndent()

        commentsSayWhyRuleAssertThat(code)
            .hasLintViolation(2, 5, MULTI_LINE_MESSAGE)
            .isFormattedAs(formattedCode)
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
            "Production code only keeps '// Why:' comments (a '/* */' block when they run over several " +
                "lines), which say why the code is the way it is: say what it does with names instead"
        const val MULTI_LINE_MESSAGE =
            "A '// Why:' that runs over several lines is a block comment: '/*' on its own line, ' * ' before " +
                "each line, and ' */' on its own line"
        const val SINGLE_LINE_MESSAGE = "A Why comment that fits on one line is a '// Why:' comment, not a block"
    }
}
