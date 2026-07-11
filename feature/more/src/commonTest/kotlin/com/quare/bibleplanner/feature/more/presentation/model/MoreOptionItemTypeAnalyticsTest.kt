package com.quare.bibleplanner.feature.more.presentation.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class MoreOptionItemTypeAnalyticsTest {
    @Test
    fun `GIVEN every more option WHEN mapping to analytics option THEN returns the documented snake_case value`() {
        // Given
        val expected = mapOf(
            MoreOptionItemType.BECOME_PRO to "become_pro",
            MoreOptionItemType.THEME to "theme",
            MoreOptionItemType.APP_LANGUAGE to "app_language",
            MoreOptionItemType.INSTAGRAM to "instagram",
            MoreOptionItemType.PRIVACY_POLICY to "privacy_policy",
            MoreOptionItemType.TERMS to "terms",
            MoreOptionItemType.DONATE to "donate",
            MoreOptionItemType.WEB_APP to "web_app",
            MoreOptionItemType.DELETE_PROGRESS to "delete_progress",
            MoreOptionItemType.EDIT_PLAN_START_DAY to "edit_plan_start_day",
            MoreOptionItemType.RELEASE_NOTES to "release_notes",
            MoreOptionItemType.BIBLE_VERSION to "bible_version",
            MoreOptionItemType.CONTACT_SUPPORT to "contact_support",
            MoreOptionItemType.RATE_APP to "rate_app",
            MoreOptionItemType.CHECK_FOR_UPDATE to "check_for_update",
        )

        // When
        val actual = MoreOptionItemType.entries.associateWith(MoreOptionItemType::toAnalyticsOption)

        // Then
        assertEquals(expected, actual)
    }
}
