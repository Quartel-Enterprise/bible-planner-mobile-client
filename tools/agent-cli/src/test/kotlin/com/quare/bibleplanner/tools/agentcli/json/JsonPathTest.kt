package com.quare.bibleplanner.tools.agentcli.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class JsonPathTest {
    private val state: JsonElement = Json.parseToJsonElement(
        """{"DayViewModel":{"uiState":{"days":[{"number":1},{"number":2}]}}}""",
    )

    @Test
    fun `selects keys and list positions`() {
        // Given
        val path = JsonPath("DayViewModel.uiState.days[1].number")

        // When
        val selected = path.select(state)

        // Then
        assertEquals(
            expected = JsonPrimitive(2),
            actual = selected,
        )
    }

    @Test
    fun `an empty path selects the whole document`() {
        // Given
        val path = JsonPath("")

        // When
        val selected = path.select(state)

        // Then
        assertEquals(
            expected = state,
            actual = selected,
        )
    }

    @Test
    fun `a missing key names the keys that exist there`() {
        // Given
        val path = JsonPath("DayViewModel.uiState.weeks")

        // When
        val error = assertFailsWith<IllegalArgumentException> { path.select(state) }

        // Then
        assertTrue("keys: [days]" in error.message.orEmpty())
    }

    @Test
    fun `a position past the end of a list fails`() {
        // Given
        val path = JsonPath("DayViewModel.uiState.days[5]")

        // When
        val error = assertFailsWith<IllegalArgumentException> { path.select(state) }

        // Then
        assertEquals(
            expected = "no item 5 at DayViewModel.uiState.days[5]",
            actual = error.message,
        )
    }
}
