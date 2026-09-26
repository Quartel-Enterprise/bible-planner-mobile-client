package com.quare.bibleplanner.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class CollectionAndBooleanUtilsTest {
    @Test
    fun `GIVEN nullable flags WHEN defaulting them THEN only null takes the default`() {
        // When
        val orFalse = listOf(true, false, null).map(Boolean?::orFalse)
        val orTrue = listOf(true, false, null).map(Boolean?::orTrue)

        // Then
        assertEquals(listOf(true, false, false), orFalse)
        assertEquals(listOf(true, false, true), orTrue)
    }

    @Test
    fun `GIVEN a list WHEN checking each index THEN only the last one is the last index`() {
        // Given
        val items = listOf("a", "b", "c")

        // When
        val flags = items.indices.map(items::isLastIndex)

        // Then
        assertEquals(listOf(false, false, true), flags)
    }
}
