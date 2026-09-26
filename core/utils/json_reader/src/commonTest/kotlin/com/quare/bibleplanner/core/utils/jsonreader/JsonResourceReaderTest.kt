package com.quare.bibleplanner.core.utils.jsonreader

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class JsonResourceReaderTest {
    private lateinit var reader: JsonResourceReader
    private lateinit var requestedPaths: MutableList<String>

    @BeforeTest
    fun setUp() {
        reader = JsonResourceReader()
        requestedPaths = mutableListOf()
    }

    @Test
    fun `GIVEN a json resource WHEN reading it THEN decodes it from the bytes of the requested path`() {
        // When
        val release: ReleaseNote = reader.read(
            path = "files/release.json",
            readBytes = ::readRelease,
        )

        // Then
        assertEquals(
            ReleaseNote(
                version = "2.8.0",
                highlights = listOf("Faster sync"),
            ),
            release,
        )
        assertEquals(listOf("files/release.json"), requestedPaths)
    }

    private fun readRelease(path: String): ByteArray {
        requestedPaths += path
        return """{"version":"2.8.0","highlights":["Faster sync"],"unknown":true}""".encodeToByteArray()
    }
}
