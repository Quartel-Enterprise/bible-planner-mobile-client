package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class TopLevelValOwnershipRuleTest {
    private val topLevelValOwnershipRuleAssertThat = assertThatRule { TopLevelValOwnershipRule() }

    @Test
    fun `GIVEN a const val only the single class in the file reads WHEN linting THEN reports the const val`() {
        // Given
        val code =
            """
            private const val PAGE_SIZE = 25

            internal class BooksRepositoryImpl {
                fun pageSize(): Int = PAGE_SIZE
            }
            """.trimIndent()

        // When
        val linted = topLevelValOwnershipRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(1, 19, buildConstantMessage("PAGE_SIZE", "BooksRepositoryImpl"))
    }

    @Test
    fun `GIVEN a val only the single class in the file reads WHEN linting THEN reports the val`() {
        // Given
        val code =
            """
            private val artworkSize = 64.dp

            class ArtworkTest {
                fun size() = artworkSize
            }
            """.trimIndent()

        // When
        val linted = topLevelValOwnershipRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(1, 13, buildValueMessage("artworkSize", "ArtworkTest"))
    }

    @Test
    fun `GIVEN a const val only an object reads WHEN linting THEN reports the const val`() {
        // Given
        val code =
            """
            private const val TAG = "Keeper"

            internal object Logger {
                fun log() = println(TAG)
            }
            """.trimIndent()

        // When
        val linted = topLevelValOwnershipRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(1, 19, buildConstantMessage("TAG", "Logger"))
    }

    @Test
    fun `GIVEN a constant a top level composable sizes itself with WHEN linting THEN reports nothing`() {
        // Given
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

        // When
        val linted = topLevelValOwnershipRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a val used as the default of a constructor parameter WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            private val defaultHoldDuration = 700.milliseconds

            class SplashViewModel(
                private val holdDuration: Duration = defaultHoldDuration,
            ) : ViewModel()
            """.trimIndent()

        // When
        val linted = topLevelValOwnershipRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a val passed to a superclass constructor call WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            private val tuneScoutAbout = About(maintainer = "Pierre Vieira")

            abstract class BiblePlannerRule(id: String) : Rule(ruleId = id, about = tuneScoutAbout)
            """.trimIndent()

        // When
        val linted = topLevelValOwnershipRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a constant two top level declarations share WHEN linting THEN reports nothing`() {
        // Given
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

        // When
        val linted = topLevelValOwnershipRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a constant in a file without a class WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            private const val DATABASE_NAME = "bible_planner.db"

            val databaseModule: Module = module {
                single { DATABASE_NAME }
            }
            """.trimIndent()

        // When
        val linted = topLevelValOwnershipRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a val an interface reads WHEN linting THEN reports nothing because an interface holds no state`() {
        // Given
        val code =
            """
            private val fallback = "none"

            interface Namer {
                fun name(): String = fallback
            }
            """.trimIndent()

        // When
        val linted = topLevelValOwnershipRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a val a value class reads WHEN linting THEN reports nothing as it cannot declare properties`() {
        // Given
        val code =
            """
            private val sizeSegment = Regex("[0-9]+x[0-9]+")

            value class Artwork(val sourceUrl: String) {
                fun hasSize(): Boolean = sizeSegment.containsMatchIn(sourceUrl)
            }
            """.trimIndent()

        // When
        val linted = topLevelValOwnershipRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a constant nothing reads WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            private const val UNUSED = 1

            class Empty
            """.trimIndent()

        // When
        val linted = topLevelValOwnershipRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a public top level val WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            val sharedPageSize = 25

            class Reader {
                fun pageSize(): Int = sharedPageSize
            }
            """.trimIndent()

        // When
        val linted = topLevelValOwnershipRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
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
