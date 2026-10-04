package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class UnusedFunctionParameterRuleTest {
    private val unusedFunctionParameterRuleAssertThat = assertThatRule { UnusedFunctionParameterRule() }

    @Test
    fun `GIVEN a parameter an expression body never reads WHEN linting THEN reports the parameter`() {
        // Given
        val code =
            """
            fun buildGreeting(name: String): String = "Hello"
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(1, 19, buildViolationMessage("name", "buildGreeting"))
    }

    @Test
    fun `GIVEN a block body reading one of two parameters WHEN linting THEN reports only the unread one`() {
        // Given
        val code =
            """
            fun play(book: Book, position: Int) {
                player.play(book)
            }
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(1, 22, buildViolationMessage("position", "play"))
    }

    @Test
    fun `GIVEN a function that reads none of its parameters WHEN linting THEN reports every parameter`() {
        // Given
        val code =
            """
            fun createBook(id: Long, title: String): Book = Book()
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasLintViolationsWithoutAutoCorrect(
            LintViolation(1, 16, buildViolationMessage("id", "createBook")),
            LintViolation(1, 26, buildViolationMessage("title", "createBook")),
        )
    }

    @Test
    fun `GIVEN a parameter whose name only labels a named argument WHEN linting THEN reports the parameter`() {
        // Given
        val code =
            """
            fun createBook(title: String): Book = Book(title = "Unknown")
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(1, 16, buildViolationMessage("title", "createBook"))
    }

    @Test
    fun `GIVEN a private method with an unused parameter WHEN linting THEN reports the parameter`() {
        // Given
        val code =
            """
            class BookMapper {
                private fun toBook(dto: BookDto, index: Int): Book = Book(dto.id)
            }
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 38, buildViolationMessage("index", "toBook"))
    }

    @Test
    fun `GIVEN a function suppressing an unrelated warning WHEN linting THEN reports the unused parameter`() {
        // Given
        val code =
            """
            @Suppress("MagicNumber")
            fun buildGreeting(name: String): String = "Hello"
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 19, buildViolationMessage("name", "buildGreeting"))
    }

    @Test
    fun `GIVEN a function that reads every parameter WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun createBook(id: Long, title: String): Book = Book(id = id, title = title)
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a parameter only a nested lambda reads WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun findBooks(query: String): List<Book> = books.filter { book -> book.title.contains(query) }
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a parameter only the default of another parameter reads WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun createRange(start: Int, end: Int = start + 10): IntRange = 0..end
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN an override whose signature the supertype dictates WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class FakePlayer : Player {
                override fun play(book: Book) {
                    println("played")
                }
            }
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN an open function whose subclass may read the parameter WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            abstract class BasePlayer {
                open fun onBookEnded(book: Book) {
                    println("ended")
                }
            }
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN functions without a body WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            abstract class BasePlayer {
                abstract fun play(book: Book)
            }

            expect fun createPlayer(context: PlatformContext): Player

            external fun decode(bytes: ByteArray): Int
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN an interface member with a default body WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            interface PlayerListener {
                fun onBookEnded(book: Book) {
                    println("ended")
                }
            }
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN an operator function WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class BookQueue {
                operator fun get(index: Int): Book = Book()
            }
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN an actual function WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            actual fun createPlayer(context: PlatformContext): Player = DesktopPlayer()
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a function suppressing the unused parameter warning WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            @Suppress("UNUSED_PARAMETER")
            fun buildGreeting(name: String): String = "Hello"
            """.trimIndent()

        // When
        val linted = unusedFunctionParameterRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }
}

private fun buildViolationMessage(
    parameterName: String,
    functionName: String,
): String = "Parameter '$parameterName' is never used by '$functionName'; remove it"
