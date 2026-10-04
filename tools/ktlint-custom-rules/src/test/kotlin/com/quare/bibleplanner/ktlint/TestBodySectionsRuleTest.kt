package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class TestBodySectionsRuleTest {
    private val testBodySectionsRuleAssertThat = assertThatRule { TestBodySectionsRule() }

    @Test
    fun `GIVEN a test without Given WHEN linting THEN reports the test`() {
        // Given
        val code =
            """
            class MapperTest {
                @Test
                fun `GIVEN no chapters WHEN formatting THEN returns an empty label`() {
                    // When
                    val label = mapper.map(emptyList())

                    // Then
                    assertEquals("", label)
                }
            }
            """.trimIndent()

        // When
        val linted = testBodySectionsRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            3,
            9,
            buildViolationMessage("GIVEN no chapters WHEN formatting THEN returns an empty label"),
        )
    }

    @Test
    fun `GIVEN a test that fuses When and Then WHEN linting THEN reports the test`() {
        // Given
        val code =
            """
            class MapperTest {
                @Test
                fun `GIVEN a blank label WHEN parsing THEN throws`() {
                    // Given
                    val label = ""

                    // When / Then
                    assertFailsWith<IllegalArgumentException> { mapper.parse(label) }
                }
            }
            """.trimIndent()

        // When
        val linted = testBodySectionsRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            3,
            9,
            buildViolationMessage("GIVEN a blank label WHEN parsing THEN throws"),
        )
    }

    @Test
    fun `GIVEN sections out of order WHEN linting THEN reports the test`() {
        // Given
        val code =
            """
            class MapperTest {
                @Test
                fun `GIVEN a label WHEN parsing THEN returns its chapters`() {
                    // When
                    val chapters = mapper.parse(label)

                    // Given
                    val label = "1-3"

                    // Then
                    assertEquals(3, chapters.size)
                }
            }
            """.trimIndent()

        // When
        val linted = testBodySectionsRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            3,
            9,
            buildViolationMessage("GIVEN a label WHEN parsing THEN returns its chapters"),
        )
    }

    @Test
    fun `GIVEN a repeated section WHEN linting THEN reports the test`() {
        // Given
        val code =
            """
            class ViewModelTest {
                @Test
                fun `GIVEN a plan WHEN opening twice THEN loads once`() = runTest {
                    // Given
                    prepareScenario()

                    // When
                    viewModel.onEvent(Open)

                    // Then
                    assertEquals(1, loads)

                    // When
                    viewModel.onEvent(Open)

                    // Then
                    assertEquals(1, loads)
                }
            }
            """.trimIndent()

        // When
        val linted = testBodySectionsRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            3,
            9,
            buildViolationMessage("GIVEN a plan WHEN opening twice THEN loads once"),
        )
    }

    @Test
    fun `GIVEN a section comment with more text WHEN linting THEN reports the test`() {
        // Given
        val code =
            """
            class MapperTest {
                @Test
                fun `GIVEN a label WHEN parsing THEN returns its chapters`() {
                    // Given
                    val label = "1-3"

                    // When
                    val chapters = mapper.parse(label)

                    // Then — one per chapter of the range
                    assertEquals(3, chapters.size)
                }
            }
            """.trimIndent()

        // When
        val linted = testBodySectionsRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            3,
            9,
            buildViolationMessage("GIVEN a label WHEN parsing THEN returns its chapters"),
        )
    }

    @Test
    fun `GIVEN a test with the three sections WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class ViewModelTest {
                @Test
                fun `GIVEN a plan WHEN opening THEN loads it`() = runTest {
                    // Given
                    prepareScenario()

                    // When
                    viewModel.onEvent(Open)

                    // Then
                    assertEquals(1, loads)
                }
            }
            """.trimIndent()

        // When
        val linted = testBodySectionsRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a class that sets up before each test WHEN linting a test without Given THEN reports nothing`() {
        // Given
        val code =
            """
            class ViewModelTest {
                @BeforeTest
                fun setUp() {
                    viewModel = ViewModel()
                }

                @Test
                fun `GIVEN a plan WHEN opening THEN loads it`() = runTest {
                    // When
                    viewModel.onEvent(Open)

                    // Then
                    assertEquals(1, loads)
                }
            }
            """.trimIndent()

        // When
        val linted = testBodySectionsRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a class that sets up before each test WHEN linting a test without Then THEN reports the test`() {
        // Given
        val code =
            """
            class ViewModelTest {
                @BeforeTest
                fun setUp() {
                    viewModel = ViewModel()
                }

                @Test
                fun `GIVEN a plan WHEN opening THEN loads it`() = runTest {
                    // When
                    viewModel.onEvent(Open)
                    assertEquals(1, loads)
                }
            }
            """.trimIndent()

        // When
        val linted = testBodySectionsRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(
            8,
            9,
            buildViolationMessage("GIVEN a plan WHEN opening THEN loads it") +
                " ('// Given' may be left out, since the class sets up before each test)",
        )
    }

    private fun buildViolationMessage(name: String): String =
        "Test '$name' splits its body with '// Given', '// When' and '// Then' comments, once each and in that order"
}
