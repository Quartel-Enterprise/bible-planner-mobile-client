package com.quare.bibleplanner.core.provider.ads.data.service

import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdFailureReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class UnsupportedAdsDataSourcesTest {
    @Test
    fun `GIVEN desktop WHEN loading a rewarded ad THEN fails without an ad`() {
        // Given
        val dataSource = UnsupportedRewardedAdDataSource()
        val failures = mutableListOf<RewardedAdFailureReason>()
        var loadCount = 0

        // When
        dataSource.load(
            adUnitId = "",
            onLoaded = { loadCount++ },
            onFailed = { failures += it },
        )

        // Then
        assertFalse(dataSource.isSupported)
        assertEquals(0, loadCount)
        assertEquals(listOf(RewardedAdFailureReason.LOAD_ERROR), failures)
    }

    @Test
    fun `GIVEN desktop WHEN showing a rewarded ad THEN fails to show`() {
        // Given
        val dataSource = UnsupportedRewardedAdDataSource()
        val outcomes = mutableListOf<String>()

        // When
        dataSource.show(
            onEarned = { outcomes += "earned" },
            onDismissed = { outcomes += "dismissed" },
            onFailed = { outcomes += "failed" },
        )

        // Then
        assertEquals(listOf("failed"), outcomes)
    }

    @Test
    fun `GIVEN desktop WHEN asking for consent THEN completes without allowing ads`() {
        // Given
        val dataSource = UnsupportedAdsConsentDataSource()
        var completions = 0

        // When
        dataSource.gatherConsent { completions++ }
        dataSource.showPrivacyOptions { completions++ }

        // Then
        assertEquals(2, completions)
        assertFalse(dataSource.canRequestAds())
        assertFalse(dataSource.isPrivacyOptionsRequired())
    }
}
