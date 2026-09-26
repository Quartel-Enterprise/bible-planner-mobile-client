package com.quare.bibleplanner.core.remoteconfig.data.dto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RemoteConfigResponseDtoTest {
    @Test
    fun `GIVEN the remote config function response WHEN decoding it THEN reads the parameters map`() {
        // Given
        val body = """{"parameters":{"show_donate":"true","web_app_url":"https://web.bibleplanner.app/"}}"""

        // When
        val dto = Json.decodeFromString(RemoteConfigResponseDto.serializer(), body)

        // Then
        assertEquals(
            mapOf(
                "show_donate" to "true",
                "web_app_url" to "https://web.bibleplanner.app/",
            ),
            dto.parameters,
        )
    }
}
