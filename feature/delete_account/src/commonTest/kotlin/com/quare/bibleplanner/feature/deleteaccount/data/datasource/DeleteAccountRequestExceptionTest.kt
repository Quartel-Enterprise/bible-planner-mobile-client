package com.quare.bibleplanner.feature.deleteaccount.data.datasource

import kotlin.test.Test
import kotlin.test.assertEquals

internal class DeleteAccountRequestExceptionTest {
    @Test
    fun `GIVEN a failed status code WHEN creating the exception THEN reports the status in its message`() {
        // When
        val exception = DeleteAccountRequestException(statusCode = 500)

        // Then
        assertEquals("The delete-account function responded with status 500", exception.message)
    }
}
