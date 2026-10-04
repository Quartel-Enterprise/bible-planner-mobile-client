package com.quare.bibleplanner.feature.deleteaccount.presentation.provider

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetConfirmationKeywordImplTest {
    @Test
    fun `GIVEN the default language WHEN reading the confirmation keyword THEN returns DELETE`() = runTest {
        // Given
        val getConfirmationKeyword = GetConfirmationKeywordImpl()

        // When
        val keyword = getConfirmationKeyword.invoke()

        // Then
        assertEquals("DELETE", keyword)
    }
}
