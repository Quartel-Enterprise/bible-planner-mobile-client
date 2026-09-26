package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class UnusedFunctionParameterRuleTest {
    private val unusedFunctionParameterRuleAssertThat = assertThatRule { UnusedFunctionParameterRule() }

    @Test
    fun `flags a parameter an expression body never reads`() {
        val code =
            """
            fun buildGreeting(name: String): String = "Hello"
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 19, buildViolationMessage("name", "buildGreeting"))
    }

    @Test
    fun `flags only the parameter a block body never reads`() {
        val code =
            """
            fun play(book: Book, position: Int) {
                player.play(book)
            }
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 22, buildViolationMessage("position", "play"))
    }

    @Test
    fun `flags every parameter the function never reads`() {
        val code =
            """
            fun createBook(id: Long, title: String): Book = Book()
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code)
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(1, 16, buildViolationMessage("id", "createBook")),
                LintViolation(1, 26, buildViolationMessage("title", "createBook")),
            )
    }

    @Test
    fun `flags a parameter whose name only labels a named argument`() {
        val code =
            """
            fun createBook(title: String): Book = Book(title = "Unknown")
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 16, buildViolationMessage("title", "createBook"))
    }

    @Test
    fun `flags an unused parameter of a private method`() {
        val code =
            """
            class BookMapper {
                private fun toBook(dto: BookDto, index: Int): Book = Book(dto.id)
            }
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 38, buildViolationMessage("index", "toBook"))
    }

    @Test
    fun `flags a function that suppresses an unrelated warning`() {
        val code =
            """
            @Suppress("MagicNumber")
            fun buildGreeting(name: String): String = "Hello"
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 19, buildViolationMessage("name", "buildGreeting"))
    }

    @Test
    fun `allows a function that reads every parameter`() {
        val code =
            """
            fun createBook(id: Long, title: String): Book = Book(id = id, title = title)
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a parameter only a nested lambda reads`() {
        val code =
            """
            fun findBooks(query: String): List<Book> = books.filter { book -> book.title.contains(query) }
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a parameter only the default of another parameter reads`() {
        val code =
            """
            fun createRange(start: Int, end: Int = start + 10): IntRange = 0..end
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows an override because the supertype dictates its signature`() {
        val code =
            """
            class FakePlayer : Player {
                override fun play(book: Book) {
                    println("played")
                }
            }
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows an open function because a subclass may read the parameter`() {
        val code =
            """
            abstract class BasePlayer {
                open fun onBookEnded(book: Book) {
                    println("ended")
                }
            }
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a function without a body`() {
        val code =
            """
            abstract class BasePlayer {
                abstract fun play(book: Book)
            }

            expect fun createPlayer(context: PlatformContext): Player

            external fun decode(bytes: ByteArray): Int
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows an interface member with a default body`() {
        val code =
            """
            interface PlayerListener {
                fun onBookEnded(book: Book) {
                    println("ended")
                }
            }
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows an operator function`() {
        val code =
            """
            class BookQueue {
                operator fun get(index: Int): Book = Book()
            }
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows an actual function`() {
        val code =
            """
            actual fun createPlayer(context: PlatformContext): Player = DesktopPlayer()
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a function that suppresses the unused parameter warning`() {
        val code =
            """
            @Suppress("UNUSED_PARAMETER")
            fun buildGreeting(name: String): String = "Hello"
            """.trimIndent()

        unusedFunctionParameterRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildViolationMessage(
    parameterName: String,
    functionName: String,
): String = "Parameter '$parameterName' is never used by '$functionName'; remove it"
