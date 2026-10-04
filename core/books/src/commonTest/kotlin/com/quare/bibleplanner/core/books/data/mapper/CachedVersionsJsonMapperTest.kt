package com.quare.bibleplanner.core.books.data.mapper

import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class CachedVersionsJsonMapperTest {
    private lateinit var mapper: CachedVersionsJsonMapper

    @BeforeTest
    fun setUp() {
        mapper = CachedVersionsJsonMapper(Json { ignoreUnknownKeys = true })
    }

    @Test
    fun `GIVEN a cache entry older than the content version WHEN mapping THEN maps it with a blank version`() {
        // Given
        val cachedJson =
            """[{"id":"ACF","name":"Almeida Corrigida Fiel","language":"pt","country":"br","chapters":1189}]"""

        // When
        val versions = mapper.map(cachedJson)

        // Then
        val version = versions.single()
        assertEquals("ACF", version.id)
        assertEquals("Almeida Corrigida Fiel", version.name)
        assertEquals("", version.version)
        assertEquals("pt", version.language)
        assertEquals("br", version.country)
        assertEquals(1189, version.chapters)
    }

    @Test
    fun `GIVEN a cache entry carrying the content version WHEN mapping THEN keeps the content version`() {
        // Given
        val cachedJson =
            """[{"id":"WEB","name":"World English Bible","version":"1.1.0","language":"en","country":"us","chapters":1189}]"""

        // When
        val versions = mapper.map(cachedJson)

        // Then
        assertEquals("1.1.0", versions.single().version)
    }

    @Test
    fun `GIVEN a cache entry with an explicit null size WHEN mapping THEN maps a null size`() {
        // Given
        val cachedJson =
            """[{"id":"ACF","name":"Almeida Corrigida Fiel","language":"pt","country":"br","chapters":1189,"size":null}]"""

        // When
        val versions = mapper.map(cachedJson)

        // Then
        assertNull(versions.single().size)
    }

    @Test
    fun `GIVEN a malformed cache entry WHEN mapping THEN fails`() {
        // Given
        val cachedJson = """[{"name":"missing id"}]"""

        // When
        val result = runCatching { mapper.map(cachedJson) }

        // Then
        assertIs<NoSuchElementException>(result.exceptionOrNull())
    }
}
