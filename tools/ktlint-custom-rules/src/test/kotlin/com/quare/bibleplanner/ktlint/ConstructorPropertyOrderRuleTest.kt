package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class ConstructorPropertyOrderRuleTest {
    private val constructorPropertyOrderRuleAssertThat = assertThatRule { ConstructorPropertyOrderRule() }

    @Test
    fun `flags a plain parameter declared between properties`() {
        val code =
            """
            class AlbumViewModel(
                private val route: AlbumRoute,
                observablePlayback: ObservablePlayback,
                private val navigator: Navigator,
            ) : ViewModel()
            """.trimIndent()

        constructorPropertyOrderRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 5, buildViolationMessage("observablePlayback"))
    }

    @Test
    fun `flags every plain parameter declared before the last property`() {
        val code =
            """
            class HomeTabsState(
                selectedIndexState: MutableIntState,
                startTab: HomeTab,
                private val backStacks: Map<HomeTab, NavBackStack<NavKey>>,
            )
            """.trimIndent()

        constructorPropertyOrderRuleAssertThat(code).hasLintViolationsWithoutAutoCorrect(
            LintViolation(2, 5, buildViolationMessage("selectedIndexState")),
            LintViolation(3, 5, buildViolationMessage("startTab")),
        )
    }

    @Test
    fun `flags a plain parameter before a public property`() {
        val code =
            """
            class Queue(
                player: ExoPlayer,
                val entries: List<QueueEntry>,
            )
            """.trimIndent()

        constructorPropertyOrderRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 5, buildViolationMessage("player"))
    }

    @Test
    fun `allows plain parameters declared after every property`() {
        val code =
            """
            class AlbumViewModel(
                private val route: AlbumRoute,
                private val navigator: Navigator,
                observablePlayback: ObservablePlayback,
                holdDuration: Duration = defaultHoldDuration,
            ) : ViewModel()
            """.trimIndent()

        constructorPropertyOrderRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a constructor with only plain parameters`() {
        val code =
            """
            class SplashViewModel(
                navigator: Navigator,
                holdDuration: Duration,
            ) : ViewModel()
            """.trimIndent()

        constructorPropertyOrderRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a constructor with only properties`() {
        val code =
            """
            data class QueueTimeline(
                val entries: List<QueueEntry>,
                val startIndex: Int,
            )
            """.trimIndent()

        constructorPropertyOrderRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a class without a primary constructor`() {
        val code =
            """
            class LibraryItemUiModelMapper {
                fun buildLibraryItems(): List<String> = emptyList()
            }
            """.trimIndent()

        constructorPropertyOrderRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildViolationMessage(name: String): String =
    "Constructor parameter '$name' is not a property and should be declared after the 'val' / 'var' properties"
