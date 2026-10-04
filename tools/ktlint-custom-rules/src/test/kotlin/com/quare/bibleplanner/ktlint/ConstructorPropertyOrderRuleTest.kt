package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class ConstructorPropertyOrderRuleTest {
    private val constructorPropertyOrderRuleAssertThat = assertThatRule { ConstructorPropertyOrderRule() }

    @Test
    fun `GIVEN a plain parameter declared between properties WHEN linting THEN reports the parameter`() {
        // Given
        val code =
            """
            class AlbumViewModel(
                private val route: AlbumRoute,
                observablePlayback: ObservablePlayback,
                private val navigator: Navigator,
            ) : ViewModel()
            """.trimIndent()

        // When
        val linted = constructorPropertyOrderRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(3, 5, buildViolationMessage("observablePlayback"))
    }

    @Test
    fun `GIVEN plain parameters declared before the last property WHEN linting THEN reports every one`() {
        // Given
        val code =
            """
            class HomeTabsState(
                selectedIndexState: MutableIntState,
                startTab: HomeTab,
                private val backStacks: Map<HomeTab, NavBackStack<NavKey>>,
            )
            """.trimIndent()

        // When
        val linted = constructorPropertyOrderRuleAssertThat(code)

        // Then
        linted.hasLintViolationsWithoutAutoCorrect(
            LintViolation(2, 5, buildViolationMessage("selectedIndexState")),
            LintViolation(3, 5, buildViolationMessage("startTab")),
        )
    }

    @Test
    fun `GIVEN a plain parameter before a public property WHEN linting THEN reports the parameter`() {
        // Given
        val code =
            """
            class Queue(
                player: ExoPlayer,
                val entries: List<QueueEntry>,
            )
            """.trimIndent()

        // When
        val linted = constructorPropertyOrderRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 5, buildViolationMessage("player"))
    }

    @Test
    fun `GIVEN plain parameters declared after every property WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class AlbumViewModel(
                private val route: AlbumRoute,
                private val navigator: Navigator,
                observablePlayback: ObservablePlayback,
                holdDuration: Duration = defaultHoldDuration,
            ) : ViewModel()
            """.trimIndent()

        // When
        val linted = constructorPropertyOrderRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a constructor with only plain parameters WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class SplashViewModel(
                navigator: Navigator,
                holdDuration: Duration,
            ) : ViewModel()
            """.trimIndent()

        // When
        val linted = constructorPropertyOrderRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a constructor with only properties WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            data class QueueTimeline(
                val entries: List<QueueEntry>,
                val startIndex: Int,
            )
            """.trimIndent()

        // When
        val linted = constructorPropertyOrderRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a class without a primary constructor WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class LibraryItemUiModelMapper {
                fun buildLibraryItems(): List<String> = emptyList()
            }
            """.trimIndent()

        // When
        val linted = constructorPropertyOrderRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }
}

private fun buildViolationMessage(name: String): String =
    "Constructor parameter '$name' is not a property and should be declared after the 'val' / 'var' properties"
