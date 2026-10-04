package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class InterfaceImplementationSeparateFilesRuleTest {
    private val separateFilesRuleAssertThat = assertThatRule { InterfaceImplementationSeparateFilesRule() }

    @Test
    fun `GIVEN a class implementing an interface declared in the same file WHEN linting THEN reports the class`() {
        // Given
        val code =
            """
            internal fun interface MediaItemFactory {
                fun createMediaItem(): Int
            }

            internal class AndroidMediaItemFactory : MediaItemFactory {
                override fun createMediaItem(): Int = 1
            }
            """.trimIndent()

        // When
        val linted = separateFilesRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            5,
            16,
            buildViolationMessage("AndroidMediaItemFactory", "MediaItemFactory"),
        )
    }

    @Test
    fun `GIVEN an object implementing an interface declared in the same file WHEN linting THEN reports the object`() {
        // Given
        val code =
            """
            interface Greeter {
                fun greet(): String
            }

            object LoudGreeter : Greeter {
                override fun greet(): String = "HI"
            }
            """.trimIndent()

        // When
        val linted = separateFilesRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(5, 8, buildViolationMessage("LoudGreeter", "Greeter"))
    }

    @Test
    fun `GIVEN an implementation listing the interface after a superclass WHEN linting THEN reports the class`() {
        // Given
        val code =
            """
            interface Launcher {
                fun launch()
            }

            class ServiceLauncher : Base(), Launcher {
                override fun launch() = Unit
            }
            """.trimIndent()

        // When
        val linted = separateFilesRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(5, 7, buildViolationMessage("ServiceLauncher", "Launcher"))
    }

    @Test
    fun `GIVEN a sealed interface with its cases in the same file WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            sealed interface AlbumUiState {
                data object Loading : AlbumUiState

                data class Loaded(val id: Long) : AlbumUiState
            }
            """.trimIndent()

        // When
        val linted = separateFilesRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a private interface next to its only implementation WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            private interface Step {
                fun run()
            }

            private class FirstStep : Step {
                override fun run() = Unit
            }
            """.trimIndent()

        // When
        val linted = separateFilesRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a default implementation nested inside the interface WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            interface Clock {
                fun now(): Long

                companion object System : Clock {
                    override fun now(): Long = 0L
                }
            }
            """.trimIndent()

        // When
        val linted = separateFilesRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a class implementing an interface from another file WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class ForegroundPlaybackServiceLauncher : PlaybackServiceLauncher {
                override fun launch() = Unit
            }
            """.trimIndent()

        // When
        val linted = separateFilesRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN an interface on its own WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun interface PlaybackServiceLauncher {
                fun launch()
            }
            """.trimIndent()

        // When
        val linted = separateFilesRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }
}

private fun buildViolationMessage(
    className: String,
    interfaceName: String,
): String = "Class '$className' implements '$interfaceName', which is declared in this same file: " +
    "keep an interface and its implementation in separate files"
