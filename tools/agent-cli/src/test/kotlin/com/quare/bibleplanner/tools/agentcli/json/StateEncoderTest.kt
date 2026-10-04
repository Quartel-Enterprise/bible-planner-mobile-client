package com.quare.bibleplanner.tools.agentcli.json

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.tools.agentcli.fake.SampleContent
import com.quare.bibleplanner.tools.agentcli.fake.SampleHolder
import com.quare.bibleplanner.tools.agentcli.fake.SampleId
import com.quare.bibleplanner.tools.agentcli.fake.SampleState
import com.quare.bibleplanner.tools.agentcli.fake.SampleTone
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.StringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

internal class StateEncoderTest {
    private val encoder = StateEncoder { resource -> "text of ${resource.key}" }

    @Test
    fun `encodes a screen state as JSON`() {
        // Given
        val state = SampleState(
            title = "Day 1",
            content = SampleContent.Loaded(
                items = listOf(1, 2, 3),
                label = null,
            ),
            tone = SampleTone.CALM,
            counts = mapOf("read" to 2),
            onClick = {},
            elapsed = 3.seconds,
            holder = SampleHolder(visible = "shown"),
            id = SampleId("GEN"),
        )

        // When
        val json = encoder.encode(
            value = state,
            maxItems = 0,
        )

        // Then
        assertEquals(
            expected = Json.parseToJsonElement(
                """
                {
                  "title": "Day 1",
                  "content": {"@type": "Loaded", "items": [1, 2, 3], "label": null},
                  "tone": "CALM",
                  "counts": {"read": 2},
                  "onClick": "<function>",
                  "elapsed": "3s",
                  "holder": {"visible": "shown"},
                  "id": "GEN"
                }
                """.trimIndent(),
            ),
            actual = json,
        )
    }

    @Test
    fun `an object is encoded as its name`() {
        // When
        val json = encoder.encode(
            value = SampleContent.Loading,
            maxItems = 0,
        )

        // Then
        assertEquals(
            expected = JsonPrimitive("Loading"),
            actual = json,
        )
    }

    @Test
    fun `long lists and maps say how many items were left out`() {
        // Given
        val state = mapOf(
            "items" to listOf(1, 2, 3),
            "counts" to mapOf("a" to 1, "b" to 2, "c" to 3),
        )

        // When
        val json = encoder.encode(
            value = state,
            maxItems = 2,
        )

        // Then
        assertEquals(
            expected = Json.parseToJsonElement(
                """{"items": [1, 2, "…more 1"], "counts": {"a": 1, "b": 2, "…more": 1}}""",
            ),
            actual = json,
        )
    }

    @Test
    fun `encodes primitives, arrays and errors`() {
        // Given
        val values = listOf(
            null,
            'x',
            true,
            intArrayOf(1),
            longArrayOf(2L),
            floatArrayOf(0.5f),
            booleanArrayOf(false),
            arrayOf("a"),
            byteArrayOf(1, 2),
            IllegalStateException("boom"),
            SampleTone::class,
        )

        // When
        val json = encoder.encode(
            value = values,
            maxItems = 0,
        )

        // Then
        assertEquals(
            expected = Json.parseToJsonElement(
                """
                [null, "x", true, [1], [2], [0.5], [false], ["a"], "<2 bytes>",
                 {"@error": "IllegalStateException", "message": "boom"}, "SampleTone"]
                """.trimIndent(),
            ),
            actual = json,
        )
    }

    @Test
    fun `a class from outside the app is encoded with its toString`() {
        // When
        val json = encoder.encode(
            value = StringBuilder("built"),
            maxItems = 0,
        )

        // Then
        assertEquals(
            expected = JsonPrimitive("built"),
            actual = json,
        )
    }

    @OptIn(InternalResourceApi::class)
    @Test
    fun `resources are encoded by key and strings with their text`() {
        // Given
        val resources = listOf(
            StringResource(
                id = "string:plans",
                key = "plans",
                items = emptySet(),
            ),
            DrawableResource(
                id = "drawable:logo",
                items = emptySet(),
            ),
            ImageVector
                .Builder(
                    name = "Filled.DateRange",
                    defaultWidth = 1.dp,
                    defaultHeight = 1.dp,
                    viewportWidth = 1f,
                    viewportHeight = 1f,
                ).build(),
        )

        // When
        val json = encoder.encode(
            value = resources,
            maxItems = 0,
        )

        // Then
        assertEquals(
            expected = Json.parseToJsonElement(
                """[{"@string": "plans", "text": "text of plans"}, "@drawable/logo", "@icon/Filled.DateRange"]""",
            ),
            actual = json,
        )
    }

    @Test
    fun `null encodes as JSON null`() {
        // When
        val json = encoder.encode(
            value = null,
            maxItems = 0,
        )

        // Then
        assertEquals(
            expected = JsonNull,
            actual = json,
        )
    }
}
