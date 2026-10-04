package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class RedundantPrivateConstructorPropertyRuleTest {
    private val redundantPrivateConstructorPropertyRuleAssertThat =
        assertThatRule { RedundantPrivateConstructorPropertyRule() }

    @Test
    fun `GIVEN a private val only a property initializer reads WHEN linting THEN reports the val`() {
        // Given
        val code =
            """
            class BooksViewModel(
                private val repository: BooksRepository,
            ) {
                val books = repository.observeBooks()
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("repository", "val"))
    }

    @Test
    fun `GIVEN a private val only an init block reads WHEN linting THEN reports the val`() {
        // Given
        val code =
            """
            class Player(
                private val volume: Int,
            ) {
                init {
                    require(volume >= 0)
                }
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("volume", "val"))
    }

    @Test
    fun `GIVEN a private val only a delegate reads WHEN linting THEN reports the val`() {
        // Given
        val code =
            """
            class BooksViewModel(
                private val repository: BooksRepository,
            ) {
                val books by lazy { repository.loadBooks() }
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("repository", "val"))
    }

    @Test
    fun `GIVEN a private val only a superclass constructor call reads WHEN linting THEN reports the val`() {
        // Given
        val code =
            """
            class BooksPagingSource(
                private val pageSize: Int,
            ) : BasePagingSource(pageSize)
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("pageSize", "val"))
    }

    @Test
    fun `GIVEN a private var only a property initializer reads WHEN linting THEN reports it naming its keyword`() {
        // Given
        val code =
            """
            class Player(
                private var volume: Int,
            ) {
                val initialVolume = volume
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("volume", "var"))
    }

    @Test
    fun `GIVEN two private vals and only one read at initialization alone WHEN linting THEN reports only that one`() {
        // Given
        val code =
            """
            class BooksViewModel(
                private val repository: BooksRepository,
                private val player: Player,
            ) {
                val books = repository.observeBooks()

                fun play() {
                    player.play()
                }
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("repository", "val"))
    }

    @Test
    fun `GIVEN a private val a method reads WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class BooksViewModel(
                private val repository: BooksRepository,
            ) {
                val books = repository.observeBooks()

                fun refresh() {
                    repository.refresh()
                }
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a private val a property getter reads WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class Player(
                private val volume: Int,
            ) {
                val isMuted: Boolean
                    get() = volume == 0
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a private val a nested class reads WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class Player(
                private val volume: Int,
            ) {
                inner class Snapshot {
                    val level = volume
                }
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a private val read through a qualifier WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class Player(
                private val volume: Int,
            ) {
                val level = this.volume
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a private val a secondary constructor reads WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class Player(
                private val volume: Int,
            ) {
                var level = 0

                constructor(volume: Int, boost: Int) : this(volume) {
                    level = volume + boost
                }
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a private val nothing reads WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class Player(
                private val volume: Int,
            )
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a private val whose name only labels a named argument WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class Player(
                private val volume: Int,
            ) {
                val mixer = Mixer(volume = 3)
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a private val of a data class WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            data class Artwork(
                private val sourceUrl: String,
            ) {
                val thumbnailUrl = sourceUrl.replace("100x100", "60x60")
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a private val of a value class WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            @JvmInline
            value class Artwork(
                private val sourceUrl: String,
            ) {
                init {
                    require(sourceUrl.isNotEmpty())
                }
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a plain parameter and a public property WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class Player(
                volume: Int,
                val name: String,
            ) {
                val level = volume
                val label = name.uppercase()
            }
            """.trimIndent()

        // When
        val linted = redundantPrivateConstructorPropertyRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }
}

private fun buildViolationMessage(
    name: String,
    keyword: String,
): String = "Constructor property '$name' is only read while the instance is initialized; drop 'private $keyword' " +
    "and keep it as a plain constructor parameter"
