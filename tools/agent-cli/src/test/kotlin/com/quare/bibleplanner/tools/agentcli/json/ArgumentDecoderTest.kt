package com.quare.bibleplanner.tools.agentcli.json

import com.quare.bibleplanner.tools.agentcli.fake.SampleContent
import com.quare.bibleplanner.tools.agentcli.fake.SampleTone
import com.quare.bibleplanner.tools.agentcli.fake.SampleUiEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ArgumentDecoderTest {
    private val decoder = ArgumentDecoder(Json)

    @Test
    fun `GIVEN an optional argument left out WHEN building the event THEN takes its default`() {
        // Given
        val arguments = "{}"

        // When
        val event = build(SampleUiEvent.OnStep::class, arguments)

        // Then
        assertEquals(
            expected = 3,
            actual = (event as SampleUiEvent.OnStep).step,
        )
    }

    @Test
    fun `GIVEN JSON arguments WHEN building events THEN builds each one`() {
        // Given
        val requests = listOf(
            SampleUiEvent.OnCount::class to """{"amount": 2}""",
            SampleUiEvent.OnTone::class to """{"tone": "LOUD"}""",
            SampleUiEvent.OnRename::class to """{}""",
            SampleUiEvent.OnItems::class to """{"items": [1, 1, 2]}""",
            SampleUiEvent.OnReset::class to """{}""",
        )

        // When
        val events = requests.map { (kClass, arguments) -> build(kClass, arguments) }

        // Then
        assertEquals(
            expected = listOf(
                SampleUiEvent.OnCount(amount = 2),
                SampleUiEvent.OnTone(tone = SampleTone.LOUD),
                SampleUiEvent.OnRename(name = null),
                SampleUiEvent.OnItems(items = setOf(1, 2)),
                SampleUiEvent.OnReset,
            ),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a sealed argument with its type name WHEN building the event THEN builds the subtype`() {
        // Given
        val argumentsList = listOf(
            """{"content": {"@type": "Loaded", "items": [4], "label": "x"}}""",
            """{"content": "Loading"}""",
        )

        // When
        val events = argumentsList.map { arguments -> build(SampleUiEvent.OnContent::class, arguments) }

        // Then
        assertEquals(
            expected = listOf(
                SampleUiEvent.OnContent(
                    content = SampleContent.Loaded(
                        items = listOf(4),
                        label = "x",
                    ),
                ),
                SampleUiEvent.OnContent(content = SampleContent.Loading),
            ),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a plain class argument WHEN building the event THEN builds it through its constructor`() {
        // Given
        val arguments = """{"holder": {"visible": "shown"}}"""

        // When
        val event = build(SampleUiEvent.OnHolder::class, arguments)

        // Then
        assertEquals(
            expected = "shown",
            actual = (event as SampleUiEvent.OnHolder).holder.visible,
        )
    }

    @Test
    fun `GIVEN a missing argument WHEN building the event THEN names the signature`() {
        // Given
        val arguments = "{}"

        // When
        val error = assertFailsWith<IllegalArgumentException> { build(SampleUiEvent.OnCount::class, arguments) }

        // Then
        assertEquals(
            expected = "missing amount: Int in (amount: Int)",
            actual = error.message,
        )
    }

    @Test
    fun `GIVEN an unknown argument WHEN building the event THEN names the signature`() {
        // Given
        val arguments = """{"amount": 1, "times": 2}"""

        // When
        val error = assertFailsWith<IllegalArgumentException> {
            build(SampleUiEvent.OnCount::class, arguments)
        }

        // Then
        assertEquals(
            expected = "<init> has no parameter times; expected (amount: Int)",
            actual = error.message,
        )
    }

    @Test
    fun `GIVEN an object event with arguments WHEN building it THEN fails`() {
        // Given
        val arguments = """{"now": true}"""

        // When
        val error = assertFailsWith<IllegalArgumentException> {
            build(SampleUiEvent.OnReset::class, arguments)
        }

        // Then
        assertEquals(
            expected = "OnReset takes no arguments",
            actual = error.message,
        )
    }

    @Test
    fun `GIVEN an unknown sealed subtype WHEN building the event THEN lists the known ones`() {
        // Given
        val arguments = """{"content": "Gone"}"""

        // When
        val error = assertFailsWith<IllegalArgumentException> {
            build(SampleUiEvent.OnContent::class, arguments)
        }

        // Then
        assertEquals(
            expected = "cannot read SampleContent from \"Gone\": Gone is not one of [Loaded, Loading]",
            actual = error.message,
        )
    }

    @Test
    fun `GIVEN a sealed argument without its type WHEN building the event THEN asks for it`() {
        // Given
        val arguments = """{"content": {"items": []}}"""

        // When
        val error = assertFailsWith<IllegalArgumentException> {
            build(SampleUiEvent.OnContent::class, arguments)
        }

        // Then
        assertEquals(
            expected = "cannot read SampleContent from {\"items\":[]}: " +
                "SampleContent is sealed: add \"@type\" with one of [Loaded, Loading]",
            actual = error.message,
        )
    }

    @Test
    fun `GIVEN a sealed hierarchy WHEN finding its concrete subclasses THEN lists every one`() {
        // Given
        val sealedClass = SampleUiEvent::class

        // When
        val subclasses = sealedClass.findConcreteSubclasses().map { it.simpleName }

        // Then
        assertEquals(
            expected = listOf("OnContent", "OnCount", "OnHolder", "OnItems", "OnRename", "OnReset", "OnStep", "OnTone"),
            actual = subclasses.sortedBy { it },
        )
    }

    private fun build(
        kClass: KClass<*>,
        arguments: String,
    ): Any = decoder.build(
        kClass = kClass,
        arguments = Json.parseToJsonElement(arguments).jsonObject,
    )
}
