package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class CommentsSayWhyRuleTest {
    private val commentsSayWhyRuleAssertThat = assertThatRule { CommentsSayWhyRule() }

    @Test
    fun `GIVEN an end-of-line comment that narrates the code WHEN linting THEN reports the comment`() {
        // Given
        val code =
            """
            fun load() {
                // fetch the books first
                fetchBooks()
            }
            """.trimIndent()

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 5, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a trailing comment WHEN linting THEN reports the comment`() {
        // Given
        val code =
            """
            val readTimestamp: Long? = null // epoch milliseconds
            """.trimIndent()

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(1, 33, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a block comment that does not start with Why WHEN linting THEN reports the comment`() {
        // Given
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

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 5, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a KDoc with tags WHEN linting THEN reports the KDoc once and not each of its tags`() {
        // Given
        val code =
            """
            /**
             * Loads the books.
             *
             * @return the books.
             */
            fun loadBooks(): List<Book> = emptyList()
            """.trimIndent()

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(1, 1, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN a Why comment WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun insets(): WindowInsets {
                // Why: CMP-10789 crashes on unattached insets when Nav3 moves a screen between scenes
                return WindowInsets.safeDrawing.asStable()
            }
            """.trimIndent()

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a multi-line Why block comment WHEN linting THEN reports nothing`() {
        // Given
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

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a Why comment continued on line comments WHEN formatting THEN turns it into a block comment`() {
        // Given
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

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted
            .hasLintViolation(2, 5, MULTI_LINE_MESSAGE)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `GIVEN a multi-line Why comment at the top of a file WHEN formatting THEN turns it into a block comment`() {
        // Given
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

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted
            .hasLintViolation(1, 1, MULTI_LINE_MESSAGE)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `GIVEN consecutive one-line Why comments WHEN linting THEN keeps them apart and reports nothing`() {
        // Given
        val code =
            """
            fun load() {
                // Why: the cache is warmed by the splash screen.
                // Why: the books must be fetched before the plans.
                fetchBooks()
            }
            """.trimIndent()

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a multi-line trailing Why comment WHEN linting THEN reports it without autocorrecting it`() {
        // Given
        val code =
            """
            fun load() {
                fetchBooks() // Why: the books must be fetched
                // before the plans.
            }
            """.trimIndent()

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 18, MULTI_LINE_MESSAGE)
    }

    @Test
    fun `GIVEN a multi-line Why comment holding comment delimiters WHEN linting THEN reports it without autocorrect`() {
        // Given
        val code =
            """
            fun load() {
                // Why: the server matches paths like /books/*
                // so the wildcard stays in the URL.
                fetchBooks()
            }
            """.trimIndent()

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 5, MULTI_LINE_MESSAGE)
    }

    @Test
    fun `GIVEN a one-line Why block comment WHEN formatting THEN turns it into a line comment`() {
        // Given
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

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted
            .hasLintViolation(2, 5, SINGLE_LINE_MESSAGE)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `GIVEN a one-line Why block comment followed by code WHEN linting THEN reports it without autocorrecting it`() {
        // Given
        val code =
            """
            fun load() {
                fetchBooks(/* Why: positional for the old API */ 1, 2)
            }
            """.trimIndent()

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 16, SINGLE_LINE_MESSAGE)
    }

    @Test
    fun `GIVEN an indented Why block comment written without stars WHEN formatting THEN keeps the inner indentation`() {
        // Given
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

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted
            .hasLintViolation(2, 5, MULTI_LINE_MESSAGE)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `GIVEN a Why block comment written without stars WHEN formatting THEN adds the stars`() {
        // Given
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

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted
            .hasLintViolation(2, 5, MULTI_LINE_MESSAGE)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `GIVEN a comment separated from a Why comment by a blank line WHEN linting THEN reports the comment`() {
        // Given
        val code =
            """
            fun load() {
                // Why: the cache is warmed by the splash screen

                // fetch the books
                fetchBooks()
            }
            """.trimIndent()

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(4, 5, VIOLATION_MESSAGE)
    }

    @Test
    fun `GIVEN several disallowed comments in a file WHEN linting THEN reports every one of them`() {
        // Given
        val code =
            """
            // header
            class Books {
                /** The count. */
                val count = 0 // zero
            }
            """.trimIndent()

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted.hasLintViolationsWithoutAutoCorrect(
            LintViolation(1, 1, VIOLATION_MESSAGE),
            LintViolation(3, 5, VIOLATION_MESSAGE),
            LintViolation(4, 19, VIOLATION_MESSAGE),
        )
    }

    @Test
    fun `GIVEN comment-like text inside a string WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            val url = "https://example.com/*path*/"
            """.trimIndent()

        // When
        val linted = commentsSayWhyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
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
