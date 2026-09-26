package com.quare.bibleplanner.feature.themeselection.data.mapper

import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ThemePreferenceMapperImplTest {
    private lateinit var mapper: ThemePreferenceMapperImpl

    @BeforeTest
    fun setUp() {
        mapper = ThemePreferenceMapperImpl()
    }

    @Test
    fun `GIVEN every theme WHEN storing and reading it back THEN returns the same theme`() {
        // Given
        val themes = Theme.entries

        // When
        val restored = themes.map { theme -> mapper.mapPreferenceToModel(mapper.mapModelToPreference(theme)) }

        // Then
        assertEquals(themes, restored)
    }

    @Test
    fun `GIVEN the dark theme WHEN storing it THEN uses the dark preference value`() {
        // When
        val preference = mapper.mapModelToPreference(Theme.DARK)

        // Then
        assertEquals("dark_theme", preference)
    }

    @Test
    fun `GIVEN no stored theme WHEN reading it THEN follows the system theme`() {
        // When
        val theme = mapper.mapPreferenceToModel(null)

        // Then
        assertEquals(Theme.SYSTEM, theme)
    }

    @Test
    fun `GIVEN every contrast WHEN storing and reading it back THEN returns the same contrast`() {
        // Given
        val contrasts = ContrastType.entries

        // When
        val restored = contrasts.map { contrast ->
            mapper.mapContrastPreferenceToModel(mapper.mapModelToContrastPreference(contrast))
        }

        // Then
        assertEquals(contrasts, restored)
    }

    @Test
    fun `GIVEN the high contrast WHEN storing it THEN uses the high contrast preference value`() {
        // When
        val preference = mapper.mapModelToContrastPreference(ContrastType.High)

        // Then
        assertEquals("high_contrast", preference)
    }

    @Test
    fun `GIVEN an unknown stored contrast WHEN reading it THEN falls back to the standard contrast`() {
        // When
        val contrast = mapper.mapContrastPreferenceToModel("ultra_contrast")

        // Then
        assertEquals(ContrastType.Standard, contrast)
    }
}
