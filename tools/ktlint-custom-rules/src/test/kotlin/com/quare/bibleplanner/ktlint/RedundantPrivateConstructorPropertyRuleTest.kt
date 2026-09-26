package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class RedundantPrivateConstructorPropertyRuleTest {
    private val redundantPrivateConstructorPropertyRuleAssertThat =
        assertThatRule { RedundantPrivateConstructorPropertyRule() }

    @Test
    fun `flags a private val only a property initializer reads`() {
        val code =
            """
            class BooksViewModel(
                private val repository: BooksRepository,
            ) {
                val books = repository.observeBooks()
            }
            """.trimIndent()

        redundantPrivateConstructorPropertyRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("repository", "val"))
    }

    @Test
    fun `flags a private val only an init block reads`() {
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

        redundantPrivateConstructorPropertyRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("volume", "val"))
    }

    @Test
    fun `flags a private val only a delegate reads`() {
        val code =
            """
            class BooksViewModel(
                private val repository: BooksRepository,
            ) {
                val books by lazy { repository.loadBooks() }
            }
            """.trimIndent()

        redundantPrivateConstructorPropertyRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("repository", "val"))
    }

    @Test
    fun `flags a private val only a superclass constructor call reads`() {
        val code =
            """
            class BooksPagingSource(
                private val pageSize: Int,
            ) : BasePagingSource(pageSize)
            """.trimIndent()

        redundantPrivateConstructorPropertyRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("pageSize", "val"))
    }

    @Test
    fun `flags a private var and names its keyword`() {
        val code =
            """
            class Player(
                private var volume: Int,
            ) {
                val initialVolume = volume
            }
            """.trimIndent()

        redundantPrivateConstructorPropertyRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("volume", "var"))
    }

    @Test
    fun `flags only the property that initialization alone reads`() {
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

        redundantPrivateConstructorPropertyRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("repository", "val"))
    }

    @Test
    fun `allows a private val a method reads`() {
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

        redundantPrivateConstructorPropertyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private val a property getter reads`() {
        val code =
            """
            class Player(
                private val volume: Int,
            ) {
                val isMuted: Boolean
                    get() = volume == 0
            }
            """.trimIndent()

        redundantPrivateConstructorPropertyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private val a nested class reads`() {
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

        redundantPrivateConstructorPropertyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private val read through a qualifier`() {
        val code =
            """
            class Player(
                private val volume: Int,
            ) {
                val level = this.volume
            }
            """.trimIndent()

        redundantPrivateConstructorPropertyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private val a secondary constructor reads`() {
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

        redundantPrivateConstructorPropertyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private val nothing reads`() {
        val code =
            """
            class Player(
                private val volume: Int,
            )
            """.trimIndent()

        redundantPrivateConstructorPropertyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private val whose name only labels a named argument`() {
        val code =
            """
            class Player(
                private val volume: Int,
            ) {
                val mixer = Mixer(volume = 3)
            }
            """.trimIndent()

        redundantPrivateConstructorPropertyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private val of a data class`() {
        val code =
            """
            data class Artwork(
                private val sourceUrl: String,
            ) {
                val thumbnailUrl = sourceUrl.replace("100x100", "60x60")
            }
            """.trimIndent()

        redundantPrivateConstructorPropertyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private val of a value class`() {
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

        redundantPrivateConstructorPropertyRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a plain parameter and a public property`() {
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

        redundantPrivateConstructorPropertyRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildViolationMessage(
    name: String,
    keyword: String,
): String = "Constructor property '$name' is only read while the instance is initialized; drop 'private $keyword' " +
    "and keep it as a plain constructor parameter"
