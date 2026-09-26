package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.bibleversion.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.bibleversion.fake.bibleModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetBibleVersionsByLanguageUseCaseTest {
    private val acf = bibleModel(
        id = "ACF",
        language = Language.PORTUGUESE_BRAZIL,
    )
    private val kjv = bibleModel(
        id = "KJV",
        language = Language.ENGLISH,
    )
    private val rvr = bibleModel(
        id = "RVR",
        language = Language.SPANISH,
    )
    private val web = bibleModel(
        id = "WEB",
        language = Language.ENGLISH,
    )

    @Test
    fun `groups the versions by language with the app language first and the rest alphabetically`() = runTest {
        // Given
        val useCase = GetBibleVersionsByLanguageUseCase(
            repository = FakeBibleRepository(listOf(kjv, acf, rvr, web)),
            getAppLanguageFlow = { flowOf(Language.SPANISH) },
        )

        // When
        val versionsByLanguage = useCase().first()

        // Then
        assertEquals(
            expected = listOf(Language.SPANISH, Language.ENGLISH, Language.PORTUGUESE_BRAZIL),
            actual = versionsByLanguage.keys.toList(),
        )
        assertEquals(
            expected = listOf(kjv, web),
            actual = versionsByLanguage[Language.ENGLISH],
        )
    }
}
