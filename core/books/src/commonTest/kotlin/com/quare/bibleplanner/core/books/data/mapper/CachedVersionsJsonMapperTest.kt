package com.quare.bibleplanner.core.books.data.mapper

import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class CachedVersionsJsonMapperTest {
    private lateinit var mapper: CachedVersionsJsonMapper

    @BeforeTest
    fun setUp() {
        mapper = CachedVersionsJsonMapper(Json { ignoreUnknownKeys = true })
    }

    @Test
    fun `maps a cache entry written before the content version existed`() {
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
    fun `maps a cache entry that carries the content version`() {
        // Given
        val cachedJson =
            """[{"id":"WEB","name":"World English Bible","version":"1.1.0","language":"en","country":"us","chapters":1189}]"""

        // When
        val versions = mapper.map(cachedJson)

        // Then
        assertEquals("1.1.0", versions.single().version)
    }

    @Test
    fun `maps a cache entry with an explicit null size`() {
        // Given
        val cachedJson =
            """[{"id":"ACF","name":"Almeida Corrigida Fiel","language":"pt","country":"br","chapters":1189,"size":null}]"""

        // When
        val versions = mapper.map(cachedJson)

        // Then
        assertNull(versions.single().size)
    }

    @Test
    fun `fails on a malformed cache entry`() {
        // Given
        val cachedJson = """[{"name":"missing id"}]"""

        // When / Then
        assertFailsWith<NoSuchElementException> {
            mapper.map(cachedJson)
        }
    }
}
