package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ExplicitBackingFieldRuleTest {
    private val explicitBackingFieldRuleAssertThat = assertThatRule { ExplicitBackingFieldRule() }

    @Test
    fun `flags a backing property exposed through asStateFlow`() {
        val code =
            """
            class Holder {
                private val _uiState = MutableStateFlow(0)
                val uiState: StateFlow<Int> = _uiState.asStateFlow()
            }
            """.trimIndent()

        explicitBackingFieldRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 17, buildViolationMessage("_uiState", "uiState"))
    }

    @Test
    fun `flags a backing property exposed through asSharedFlow`() {
        val code =
            """
            class Holder {
                private val _uiAction = MutableSharedFlow<String>()
                val uiAction: SharedFlow<String> = _uiAction.asSharedFlow()
            }
            """.trimIndent()

        explicitBackingFieldRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 17, buildViolationMessage("_uiAction", "uiAction"))
    }

    @Test
    fun `flags a backing property assigned directly to an overriding property declared before it`() {
        val code =
            """
            class Repository : Source {
                override val selection: StateFlow<String?> = _selection

                private val _selection = MutableStateFlow<String?>(null)
            }
            """.trimIndent()

        explicitBackingFieldRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 17, buildViolationMessage("_selection", "selection"))
    }

    @Test
    fun `does not flag a channel exposed through receiveAsFlow`() {
        val code =
            """
            class Navigator {
                private val _commands = Channel<String>(Channel.BUFFERED)
                val commands: Flow<String> = _commands.receiveAsFlow()
            }
            """.trimIndent()

        explicitBackingFieldRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag an explicit backing field`() {
        val code =
            """
            class Holder {
                val uiState: StateFlow<Int>
                    field = MutableStateFlow<Int>(0)
            }
            """.trimIndent()

        explicitBackingFieldRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag a property exposing a backing property under a different name`() {
        val code =
            """
            class Holder {
                private val _state = MutableStateFlow(0)
                val uiState: StateFlow<Int> = _state.asStateFlow()
            }
            """.trimIndent()

        explicitBackingFieldRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag a mutable backing var`() {
        val code =
            """
            class Holder {
                private var _count = 0
                val count: Int = _count
            }
            """.trimIndent()

        explicitBackingFieldRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `does not flag top level properties`() {
        val code =
            """
            private val _uiState = MutableStateFlow(0)
            val uiState: StateFlow<Int> = _uiState.asStateFlow()
            """.trimIndent()

        explicitBackingFieldRuleAssertThat(code).hasNoLintViolations()
    }

    private fun buildViolationMessage(
        backingName: String,
        name: String,
    ): String = "Backing property '$backingName' only exposes '$name' — declare '$name' with an explicit backing " +
        "field (field = ...) instead"
}
