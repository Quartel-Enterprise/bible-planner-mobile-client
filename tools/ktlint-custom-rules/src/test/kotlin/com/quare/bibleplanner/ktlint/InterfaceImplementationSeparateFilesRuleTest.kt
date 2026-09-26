package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class InterfaceImplementationSeparateFilesRuleTest {
    private val separateFilesRuleAssertThat = assertThatRule { InterfaceImplementationSeparateFilesRule() }

    @Test
    fun `flags a class implementing an interface declared in the same file`() {
        val code =
            """
            internal fun interface MediaItemFactory {
                fun createMediaItem(): Int
            }

            internal class AndroidMediaItemFactory : MediaItemFactory {
                override fun createMediaItem(): Int = 1
            }
            """.trimIndent()

        separateFilesRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(
                5,
                16,
                buildViolationMessage("AndroidMediaItemFactory", "MediaItemFactory"),
            )
    }

    @Test
    fun `flags an object implementing an interface declared in the same file`() {
        val code =
            """
            interface Greeter {
                fun greet(): String
            }

            object LoudGreeter : Greeter {
                override fun greet(): String = "HI"
            }
            """.trimIndent()

        separateFilesRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(5, 8, buildViolationMessage("LoudGreeter", "Greeter"))
    }

    @Test
    fun `flags the implementation even when the interface comes after a superclass`() {
        val code =
            """
            interface Launcher {
                fun launch()
            }

            class ServiceLauncher : Base(), Launcher {
                override fun launch() = Unit
            }
            """.trimIndent()

        separateFilesRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(5, 7, buildViolationMessage("ServiceLauncher", "Launcher"))
    }

    @Test
    fun `allows a sealed interface with its cases in the same file`() {
        val code =
            """
            sealed interface AlbumUiState {
                data object Loading : AlbumUiState

                data class Loaded(val id: Long) : AlbumUiState
            }
            """.trimIndent()

        separateFilesRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private interface next to its only implementation`() {
        val code =
            """
            private interface Step {
                fun run()
            }

            private class FirstStep : Step {
                override fun run() = Unit
            }
            """.trimIndent()

        separateFilesRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a default implementation nested inside the interface`() {
        val code =
            """
            interface Clock {
                fun now(): Long

                companion object System : Clock {
                    override fun now(): Long = 0L
                }
            }
            """.trimIndent()

        separateFilesRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a class implementing an interface from another file`() {
        val code =
            """
            class ForegroundPlaybackServiceLauncher : PlaybackServiceLauncher {
                override fun launch() = Unit
            }
            """.trimIndent()

        separateFilesRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows an interface on its own`() {
        val code =
            """
            fun interface PlaybackServiceLauncher {
                fun launch()
            }
            """.trimIndent()

        separateFilesRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildViolationMessage(
    className: String,
    interfaceName: String,
): String = "Class '$className' implements '$interfaceName', which is declared in this same file: " +
    "keep an interface and its implementation in separate files"
