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
    fun `an optional argument left out takes its default`() {
        // When
        val event = build(SampleUiEvent.OnStep::class, "{}")

        // Then
        assertEquals(
            expected = 3,
            actual = (event as SampleUiEvent.OnStep).step,
        )
    }

    @Test
    fun `builds events from their JSON arguments`() {
        // When
        val events = listOf(
            build(SampleUiEvent.OnCount::class, """{"amount": 2}"""),
            build(SampleUiEvent.OnTone::class, """{"tone": "LOUD"}"""),
            build(SampleUiEvent.OnRename::class, """{}"""),
            build(SampleUiEvent.OnItems::class, """{"items": [1, 1, 2]}"""),
            build(SampleUiEvent.OnReset::class, """{}"""),
        )

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
    fun `builds a sealed argument from its type name`() {
        // When
        val events = listOf(
            build(
                SampleUiEvent.OnContent::class,
                """{"content": {"@type": "Loaded", "items": [4], "label": "x"}}""",
            ),
            build(SampleUiEvent.OnContent::class, """{"content": "Loading"}"""),
        )

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
    fun `builds a plain class argument through its constructor`() {
        // When
        val event = build(SampleUiEvent.OnHolder::class, """{"holder": {"visible": "shown"}}""")

        // Then
        assertEquals(
            expected = "shown",
            actual = (event as SampleUiEvent.OnHolder).holder.visible,
        )
    }

    @Test
    fun `a missing argument names the signature`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> { build(SampleUiEvent.OnCount::class, "{}") }

        // Then
        assertEquals(
            expected = "missing amount: Int in (amount: Int)",
            actual = error.message,
        )
    }

    @Test
    fun `an unknown argument names the signature`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> {
            build(SampleUiEvent.OnCount::class, """{"amount": 1, "times": 2}""")
        }

        // Then
        assertEquals(
            expected = "<init> has no parameter times; expected (amount: Int)",
            actual = error.message,
        )
    }

    @Test
    fun `an object takes no arguments`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> {
            build(SampleUiEvent.OnReset::class, """{"now": true}""")
        }

        // Then
        assertEquals(
            expected = "OnReset takes no arguments",
            actual = error.message,
        )
    }

    @Test
    fun `an unknown sealed subtype lists the known ones`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> {
            build(SampleUiEvent.OnContent::class, """{"content": "Gone"}""")
        }

        // Then
        assertEquals(
            expected = "cannot read SampleContent from \"Gone\": Gone is not one of [Loaded, Loading]",
            actual = error.message,
        )
    }

    @Test
    fun `a sealed argument without its type asks for it`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> {
            build(SampleUiEvent.OnContent::class, """{"content": {"items": []}}""")
        }

        // Then
        assertEquals(
            expected = "cannot read SampleContent from {\"items\":[]}: " +
                "SampleContent is sealed: add \"@type\" with one of [Loaded, Loading]",
            actual = error.message,
        )
    }

    @Test
    fun `lists the concrete subclasses of a sealed hierarchy`() {
        // When
        val subclasses = SampleUiEvent::class.findConcreteSubclasses().map { it.simpleName }

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
