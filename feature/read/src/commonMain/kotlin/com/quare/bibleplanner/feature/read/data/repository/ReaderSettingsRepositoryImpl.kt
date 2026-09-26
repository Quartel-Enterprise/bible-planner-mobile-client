package com.quare.bibleplanner.feature.read.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.domain.repository.ReaderSettingsRepository
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ReaderSettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : ReaderSettingsRepository {
    private val fontSizeKey = floatPreferencesKey("reader_font_size")
    private val fontKey = stringPreferencesKey("reader_font")
    private val rulerEnabledKey = booleanPreferencesKey("reader_ruler_enabled")
    private val rulerLinesKey = intPreferencesKey("reader_ruler_lines")
    private val focusedVerseEnabledKey = booleanPreferencesKey("reader_focused_verse_enabled")
    private val verticalReadingEnabledKey = booleanPreferencesKey("reader_vertical_reading_enabled")

    override fun observe(): Flow<ReaderSettingsModel> = dataStore.data.map { preferences ->
        ReaderSettingsModel(
            fontSizeSp = preferences[fontSizeKey] ?: ReaderFontSize.DEFAULT,
            font = preferences[fontKey]?.toReaderFont() ?: ReaderFont.LORA,
            isRulerEnabled = preferences[rulerEnabledKey] == true,
            rulerLines = preferences[rulerLinesKey] ?: ReaderRulerLines.DEFAULT,
            isFocusedVerseEnabled = preferences[focusedVerseEnabledKey] == true,
            isVerticalReadingEnabled = preferences[verticalReadingEnabledKey] == true,
        )
    }

    override suspend fun setFontSize(fontSizeSp: Float) = dataStore.write(
        key = fontSizeKey,
        value = fontSizeSp,
    )

    override suspend fun setFont(fontName: String) = dataStore.write(
        key = fontKey,
        value = fontName,
    )

    override suspend fun setRulerEnabled(isEnabled: Boolean) = dataStore.write(
        key = rulerEnabledKey,
        value = isEnabled,
    )

    override suspend fun setRulerLines(lines: Int) = dataStore.write(
        key = rulerLinesKey,
        value = lines,
    )

    override suspend fun setFocusedVerseEnabled(isEnabled: Boolean) = dataStore.write(
        key = focusedVerseEnabledKey,
        value = isEnabled,
    )

    override suspend fun setVerticalReadingEnabled(isEnabled: Boolean) = dataStore.write(
        key = verticalReadingEnabledKey,
        value = isEnabled,
    )

    private fun String.toReaderFont(): ReaderFont? = ReaderFont.entries.find { it.name == this }
}
