package com.quare.bibleplanner.core.provider.ads.data.service

import com.quare.bibleplanner.core.provider.ads.domain.service.AdsConsentDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class AdsConsentServiceImplTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var service: AdsConsentServiceImpl
    private lateinit var dataSource: FakeAdsConsentDataSource

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN consent was already gathered WHEN gathering again THEN the form is requested once`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPrivacyOptionsRequired = false)
            service.gatherConsent()

            // When
            service.gatherConsent()

            // Then
            assertEquals(1, dataSource.gatherCount)
        }

    @Test
    fun `GIVEN the consent info could not be updated WHEN gathering again THEN asks again`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            isPrivacyOptionsRequired = false,
            isInfoUpdated = false,
        )
        service.gatherConsent()

        // When
        service.gatherConsent()

        // Then
        assertEquals(2, dataSource.gatherCount)
    }

    @Test
    fun `GIVEN privacy options are required WHEN consent is gathered THEN exposes the requirement`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPrivacyOptionsRequired = true)

            // When
            service.gatherConsent()

            // Then
            assertTrue(service.isPrivacyOptionsRequired.value)
        }

    @Test
    fun `GIVEN the user withdraws from privacy options WHEN the form closes THEN refreshes the requirement`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPrivacyOptionsRequired = true)
            service.gatherConsent()
            dataSource.privacyOptionsRequirement = false

            // When
            service.showPrivacyOptions()

            // Then
            assertEquals(1, dataSource.privacyOptionsShownCount)
            assertFalse(service.isPrivacyOptionsRequired.value)
        }

    @Test
    fun `GIVEN the consent SDK allows ads WHEN asking THEN delegates to it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isPrivacyOptionsRequired = false)

        // When
        val canRequestAds = service.canRequestAds()

        // Then
        assertTrue(canRequestAds)
    }

    private fun prepareScenario(
        isPrivacyOptionsRequired: Boolean,
        isInfoUpdated: Boolean = true,
    ) {
        dataSource = FakeAdsConsentDataSource(
            privacyOptionsRequirement = isPrivacyOptionsRequired,
            isInfoUpdated = isInfoUpdated,
        )
        service = AdsConsentServiceImpl(dataSource = dataSource)
    }
}

private class FakeAdsConsentDataSource(
    var privacyOptionsRequirement: Boolean,
    var isInfoUpdated: Boolean,
) : AdsConsentDataSource {
    var gatherCount = 0
    var privacyOptionsShownCount = 0

    override fun gatherConsent(onComplete: (isInfoUpdated: Boolean) -> Unit) {
        gatherCount++
        onComplete(isInfoUpdated)
    }

    override fun canRequestAds(): Boolean = true

    override fun isPrivacyOptionsRequired(): Boolean = privacyOptionsRequirement

    override fun showPrivacyOptions(onComplete: () -> Unit) {
        privacyOptionsShownCount++
        onComplete()
    }
}
