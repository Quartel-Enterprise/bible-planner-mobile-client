package com.quare.bibleplanner.feature.chat.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.quare.bibleplanner.core.datastore.read
import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel

internal class ChatStudyQuestionsLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    private val daysKey = stringSetPreferencesKey(STUDY_QUESTION_DAYS_KEY)

    suspend fun remember(planDay: ChatPlanDayModel) {
        dataStore.edit { preferences ->
            preferences[daysKey] = preferences[daysKey].orEmpty() + planDay.key()
        }
    }

    suspend fun contains(planDay: ChatPlanDayModel): Boolean = dataStore.read(daysKey).orEmpty().contains(planDay.key())

    suspend fun clear() {
        dataStore.edit { preferences -> preferences.remove(daysKey) }
    }

    private fun ChatPlanDayModel.key(): String = "$readingPlanType:$weekNumber:$dayNumber"

    private companion object {
        const val STUDY_QUESTION_DAYS_KEY = "chat_study_question_days"
    }
}
