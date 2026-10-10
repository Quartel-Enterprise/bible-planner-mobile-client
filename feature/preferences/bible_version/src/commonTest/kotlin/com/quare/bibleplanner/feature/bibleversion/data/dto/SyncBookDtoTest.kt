package com.quare.bibleplanner.feature.bibleversion.data.dto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SyncBookDtoTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Test
    fun `GIVEN a book file as bible-versions publishes it WHEN decoding it THEN reads every chapter`() {
        // Given
        val body = """{"version":"ACF","book":"Sl","chapters":[""" +
            """{"version":"ACF","book":"Sl","chapter":3,"superscription":"Salmo de Davi","verses":[""" +
            """{"number":1,"heading":"Confiança","text":"SENHOR, como se têm multiplicado","styles":[""" +
            """{"substring":"SENHOR","type":"smallCaps"}]}]}]}"""

        // When
        val dto = json.decodeFromString(SyncBookDto.serializer(), body)

        // Then
        assertEquals(
            expected = SyncBookDto(
                chapters = listOf(
                    SyncChapterDto(
                        chapter = 3,
                        verses = listOf(
                            SyncVerseDto(
                                number = 1,
                                text = "SENHOR, como se têm multiplicado",
                                heading = "Confiança",
                            ),
                        ),
                    ),
                ),
            ),
            actual = dto,
        )
    }
}
