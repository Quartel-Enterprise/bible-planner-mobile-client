package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ExplicitBackingFieldRuleTest {
    private val explicitBackingFieldRuleAssertThat = assertThatRule { ExplicitBackingFieldRule() }

    @Test
    fun `GIVEN a backing property exposed through asStateFlow WHEN linting THEN reports it`() {
        // Given
        val code =
            """
            class Holder {
                private val _uiState = MutableStateFlow(0)
                val uiState: StateFlow<Int> = _uiState.asStateFlow()
            }
            """.trimIndent()

        // When
        val linted = explicitBackingFieldRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 17, buildViolationMessage("_uiState", "uiState"))
    }

    @Test
    fun `GIVEN a backing property exposed through asSharedFlow WHEN linting THEN reports it`() {
        // Given
        val code =
            """
            class Holder {
                private val _uiAction = MutableSharedFlow<String>()
                val uiAction: SharedFlow<String> = _uiAction.asSharedFlow()
            }
            """.trimIndent()

        // When
        val linted = explicitBackingFieldRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 17, buildViolationMessage("_uiAction", "uiAction"))
    }

    @Test
    fun `GIVEN a backing property assigned directly to an earlier override WHEN linting THEN reports it`() {
        // Given
        val code =
            """
            class Repository : Source {
                override val selection: StateFlow<String?> = _selection

                private val _selection = MutableStateFlow<String?>(null)
            }
            """.trimIndent()

        // When
        val linted = explicitBackingFieldRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(4, 17, buildViolationMessage("_selection", "selection"))
    }

    @Test
    fun `GIVEN a channel exposed through receiveAsFlow WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class Navigator {
                private val _commands = Channel<String>(Channel.BUFFERED)
                val commands: Flow<String> = _commands.receiveAsFlow()
            }
            """.trimIndent()

        // When
        val linted = explicitBackingFieldRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN an explicit backing field WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class Holder {
                val uiState: StateFlow<Int>
                    field = MutableStateFlow<Int>(0)
            }
            """.trimIndent()

        // When
        val linted = explicitBackingFieldRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a property exposing a backing property under a different name WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class Holder {
                private val _state = MutableStateFlow(0)
                val uiState: StateFlow<Int> = _state.asStateFlow()
            }
            """.trimIndent()

        // When
        val linted = explicitBackingFieldRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a mutable backing var WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            class Holder {
                private var _count = 0
                val count: Int = _count
            }
            """.trimIndent()

        // When
        val linted = explicitBackingFieldRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN top level backing properties WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            private val _uiState = MutableStateFlow(0)
            val uiState: StateFlow<Int> = _uiState.asStateFlow()
            """.trimIndent()

        // When
        val linted = explicitBackingFieldRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    private fun buildViolationMessage(
        backingName: String,
        name: String,
    ): String = "Backing property '$backingName' only exposes '$name' — declare '$name' with an explicit backing " +
        "field (field = ...) instead"
}
