package com.quare.bibleplanner.feature.profile.presentation.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ProfileOptionItemTypeAnalyticsTest {
    @Test
    fun `GIVEN every profile option WHEN mapping to analytics option THEN returns the documented snake_case value`() {
        // Given
        val expected = mapOf(
            ProfileOptionItemType.BECOME_PRO to "become_pro",
            ProfileOptionItemType.THEME to "theme",
            ProfileOptionItemType.APP_LANGUAGE to "app_language",
            ProfileOptionItemType.INSTAGRAM to "instagram",
            ProfileOptionItemType.PRIVACY_POLICY to "privacy_policy",
            ProfileOptionItemType.TERMS to "terms",
            ProfileOptionItemType.DONATE to "donate",
            ProfileOptionItemType.WEB_APP to "web_app",
            ProfileOptionItemType.DELETE_PROGRESS to "delete_progress",
            ProfileOptionItemType.EDIT_PLAN_START_DAY to "edit_plan_start_day",
            ProfileOptionItemType.RELEASE_NOTES to "release_notes",
            ProfileOptionItemType.BIBLE_VERSION to "bible_version",
            ProfileOptionItemType.CONTACT_SUPPORT to "contact_support",
            ProfileOptionItemType.RATE_APP to "rate_app",
            ProfileOptionItemType.CHECK_FOR_UPDATE to "check_for_update",
        )

        // When
        val actual = ProfileOptionItemType.entries.associateWith(ProfileOptionItemType::toAnalyticsOption)

        // Then
        assertEquals(expected, actual)
    }
}
