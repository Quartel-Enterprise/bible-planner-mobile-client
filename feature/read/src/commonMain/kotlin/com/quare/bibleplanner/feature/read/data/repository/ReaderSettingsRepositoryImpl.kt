package com.quare.bibleplanner.feature.read.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.domain.repository.ReaderSettingsRepository
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ReaderSettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : ReaderSettingsRepository {
    override fun observe(): Flow<ReaderSettingsModel> = dataStore.data.map { preferences ->
        ReaderSettingsModel(
            fontSizeSp = preferences[FONT_SIZE_KEY] ?: ReaderFontSize.DEFAULT,
            font = preferences[FONT_KEY]?.toReaderFont() ?: ReaderFont.LORA,
            isRulerEnabled = preferences[RULER_ENABLED_KEY] == true,
            isFocusedVerseEnabled = preferences[FOCUSED_VERSE_ENABLED_KEY] == true,
            isVerticalReadingEnabled = preferences[VERTICAL_READING_ENABLED_KEY] == true,
        )
    }

    override suspend fun setFontSize(fontSizeSp: Float) = dataStore.write(
        key = FONT_SIZE_KEY,
        value = fontSizeSp,
    )

    override suspend fun setFont(fontName: String) = dataStore.write(
        key = FONT_KEY,
        value = fontName,
    )

    override suspend fun setRulerEnabled(isEnabled: Boolean) = dataStore.write(
        key = RULER_ENABLED_KEY,
        value = isEnabled,
    )

    override suspend fun setFocusedVerseEnabled(isEnabled: Boolean) = dataStore.write(
        key = FOCUSED_VERSE_ENABLED_KEY,
        value = isEnabled,
    )

    override suspend fun setVerticalReadingEnabled(isEnabled: Boolean) = dataStore.write(
        key = VERTICAL_READING_ENABLED_KEY,
        value = isEnabled,
    )

    private fun String.toReaderFont(): ReaderFont? = ReaderFont.entries.find { it.name == this }

    private companion object {
        val FONT_SIZE_KEY = floatPreferencesKey("reader_font_size")
        val FONT_KEY = stringPreferencesKey("reader_font")
        val RULER_ENABLED_KEY = booleanPreferencesKey("reader_ruler_enabled")
        val FOCUSED_VERSE_ENABLED_KEY = booleanPreferencesKey("reader_focused_verse_enabled")
        val VERTICAL_READING_ENABLED_KEY = booleanPreferencesKey("reader_vertical_reading_enabled")
    }
}
