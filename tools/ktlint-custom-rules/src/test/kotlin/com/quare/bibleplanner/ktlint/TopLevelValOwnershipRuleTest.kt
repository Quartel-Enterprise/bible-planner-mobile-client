package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class TopLevelValOwnershipRuleTest {
    private val topLevelValOwnershipRuleAssertThat = assertThatRule { TopLevelValOwnershipRule() }

    @Test
    fun `flags a const val only the single class in the file reads`() {
        val code =
            """
            private const val PAGE_SIZE = 25

            internal class BooksRepositoryImpl {
                fun pageSize(): Int = PAGE_SIZE
            }
            """.trimIndent()

        topLevelValOwnershipRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 19, buildConstantMessage("PAGE_SIZE", "BooksRepositoryImpl"))
    }

    @Test
    fun `flags a val only the single class in the file reads`() {
        val code =
            """
            private val artworkSize = 64.dp

            class ArtworkTest {
                fun size() = artworkSize
            }
            """.trimIndent()

        topLevelValOwnershipRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 13, buildValueMessage("artworkSize", "ArtworkTest"))
    }

    @Test
    fun `flags a const val only an object reads`() {
        val code =
            """
            private const val TAG = "Keeper"

            internal object Logger {
                fun log() = println(TAG)
            }
            """.trimIndent()

        topLevelValOwnershipRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 19, buildConstantMessage("TAG", "Logger"))
    }

    @Test
    fun `allows a constant a top level composable sizes itself with`() {
        val code =
            """
            private val rowCornerRadius = 8.dp

            class Unrelated {
                fun noop() = Unit
            }

            @Composable
            fun BookRow() {
                Box(modifier = Modifier.clip(RoundedCornerShape(rowCornerRadius)))
            }
            """.trimIndent()

        topLevelValOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a default for a constructor parameter`() {
        val code =
            """
            private val defaultHoldDuration = 700.milliseconds

            class SplashViewModel(
                private val holdDuration: Duration = defaultHoldDuration,
            ) : ViewModel()
            """.trimIndent()

        topLevelValOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows an argument to a superclass constructor call`() {
        val code =
            """
            private val tuneScoutAbout = About(maintainer = "Pierre Vieira")

            abstract class BiblePlannerRule(id: String) : Rule(ruleId = id, about = tuneScoutAbout)
            """.trimIndent()

        topLevelValOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a constant two top level declarations share`() {
        val code =
            """
            private const val PAGE_SIZE = 25

            class SearchBooksPagingSourceTest {
                fun pageSize(): Int = PAGE_SIZE
            }

            private class FakeRemoteDataSource {
                fun pageSize(): Int = PAGE_SIZE
            }
            """.trimIndent()

        topLevelValOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a constant in a file without a class`() {
        val code =
            """
            private const val DATABASE_NAME = "bible_planner.db"

            val databaseModule: Module = module {
                single { DATABASE_NAME }
            }
            """.trimIndent()

        topLevelValOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a val an interface reads because an interface holds no state`() {
        val code =
            """
            private val fallback = "none"

            interface Namer {
                fun name(): String = fallback
            }
            """.trimIndent()

        topLevelValOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a val a value class reads because it cannot declare properties`() {
        val code =
            """
            private val sizeSegment = Regex("[0-9]+x[0-9]+")

            value class Artwork(val sourceUrl: String) {
                fun hasSize(): Boolean = sizeSegment.containsMatchIn(sourceUrl)
            }
            """.trimIndent()

        topLevelValOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a constant nothing reads`() {
        val code =
            """
            private const val UNUSED = 1

            class Empty
            """.trimIndent()

        topLevelValOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a public top level val`() {
        val code =
            """
            val sharedPageSize = 25

            class Reader {
                fun pageSize(): Int = sharedPageSize
            }
            """.trimIndent()

        topLevelValOwnershipRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildConstantMessage(
    name: String,
    ownerName: String,
): String = "Top-level private const val '$name' is only read by '$ownerName' and should be declared in its " +
    "private companion object"

private fun buildValueMessage(
    name: String,
    ownerName: String,
): String = "Top-level private val '$name' is only read by '$ownerName' and should be declared in its class body"
