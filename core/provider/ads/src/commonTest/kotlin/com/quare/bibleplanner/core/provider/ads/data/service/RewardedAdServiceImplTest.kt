package com.quare.bibleplanner.core.provider.ads.data.service

import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdAvailability
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdFailureReason
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdResult
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdDataSource
import com.quare.bibleplanner.core.provider.ads.testing.FakeAdsConsentService
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
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
internal class RewardedAdServiceImplTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var service: RewardedAdServiceImpl
    private lateinit var dataSource: FakeRewardedAdDataSource
    private lateinit var consentService: FakeAdsConsentService

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN an unsupported platform WHEN preloading THEN nothing is requested`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isPlatformSupported = false)

        // When
        service.preload()

        // Then
        assertFalse(service.isSupported)
        assertEquals(RewardedAdAvailability.IDLE, service.availability.value)
        assertEquals(0, consentService.gatherCount)
        assertTrue(dataSource.loadedAdUnitIds.isEmpty())
    }

    @Test
    fun `GIVEN a blank ad unit WHEN checking support THEN rewarded ads are unsupported`() = runTest(testDispatcher) {
        // Given
        prepareScenario(adUnitId = "")

        // When
        service.preload()

        // Then
        assertFalse(service.isSupported)
        assertTrue(dataSource.loadedAdUnitIds.isEmpty())
    }

    @Test
    fun `GIVEN consent allows ads WHEN preloading THEN gathers consent loads the unit and becomes ready`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            service.preload()

            // Then
            assertEquals(1, consentService.gatherCount)
            assertEquals(listOf(AD_UNIT_ID), dataSource.loadedAdUnitIds)
            assertEquals(RewardedAdAvailability.READY, service.availability.value)
        }

    @Test
    fun `GIVEN a ready ad WHEN preloading again THEN it is not reloaded`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        service.preload()

        // When
        service.preload()

        // Then
        assertEquals(1, dataSource.loadedAdUnitIds.size)
    }

    @Test
    fun `GIVEN consent forbids ads WHEN showing THEN fails as a load error without loading`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(canRequestAds = false)
            service.preload()

            // When
            val result = service.show()

            // Then
            assertTrue(dataSource.loadedAdUnitIds.isEmpty())
            assertEquals(RewardedAdAvailability.LOAD_ERROR, service.availability.value)
            assertEquals(RewardedAdResult.Failed(RewardedAdFailureReason.LOAD_ERROR), result)
        }

    @Test
    fun `GIVEN no fill WHEN showing THEN fails as no fill`() = runTest(testDispatcher) {
        // Given
        prepareScenario(loadFailure = RewardedAdFailureReason.NO_FILL)
        service.preload()

        // When
        val result = service.show()

        // Then
        assertEquals(RewardedAdAvailability.NO_FILL, service.availability.value)
        assertEquals(RewardedAdResult.Failed(RewardedAdFailureReason.NO_FILL), result)
    }

    @Test
    fun `GIVEN a failed load WHEN preloading again THEN retries the load`() = runTest(testDispatcher) {
        // Given
        prepareScenario(loadFailure = RewardedAdFailureReason.LOAD_ERROR)
        service.preload()

        // When
        service.preload()

        // Then
        assertEquals(2, dataSource.loadedAdUnitIds.size)
        assertEquals(RewardedAdAvailability.LOAD_ERROR, service.availability.value)
    }

    @Test
    fun `GIVEN nothing was preloaded WHEN showing THEN fails as a load error`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val result = service.show()

        // Then
        assertEquals(RewardedAdResult.Failed(RewardedAdFailureReason.LOAD_ERROR), result)
    }

    @Test
    fun `GIVEN the user earns the reward WHEN the ad is dismissed THEN returns earned`() = runTest(testDispatcher) {
        // Given
        prepareScenario(showOutcome = ShowOutcome.EARNED)
        service.preload()

        // When
        val result = service.show()

        // Then
        assertEquals(RewardedAdResult.Earned, result)
        assertEquals(RewardedAdAvailability.IDLE, service.availability.value)
    }

    @Test
    fun `GIVEN the user closes the ad early WHEN showing THEN returns dismissed`() = runTest(testDispatcher) {
        // Given
        prepareScenario(showOutcome = ShowOutcome.DISMISSED)
        service.preload()

        // When
        val result = service.show()

        // Then
        assertEquals(RewardedAdResult.Dismissed, result)
    }

    @Test
    fun `GIVEN the ad fails to show WHEN showing THEN fails as a show error`() = runTest(testDispatcher) {
        // Given
        prepareScenario(showOutcome = ShowOutcome.FAILED)
        service.preload()

        // When
        val result = service.show()

        // Then
        assertEquals(RewardedAdResult.Failed(RewardedAdFailureReason.SHOW_ERROR), result)
    }

    private fun TestScope.prepareScenario(
        isPlatformSupported: Boolean = true,
        adUnitId: String = AD_UNIT_ID,
        canRequestAds: Boolean = true,
        loadFailure: RewardedAdFailureReason? = null,
        showOutcome: ShowOutcome = ShowOutcome.EARNED,
    ) {
        dataSource = FakeRewardedAdDataSource(
            isSupported = isPlatformSupported,
            loadFailure = loadFailure,
            showOutcome = showOutcome,
        )
        consentService = FakeAdsConsentService(
            isPrivacyOptionsRequired = false,
            canRequestAds = canRequestAds,
        )
        service = RewardedAdServiceImpl(
            dataSource = dataSource,
            consentService = consentService,
            adUnitIdProvider = { adUnitId },
            applicationScope = ApplicationScope(this),
        )
    }

    private companion object {
        const val AD_UNIT_ID = "ad-unit"
    }
}

private class FakeRewardedAdDataSource(
    override val isSupported: Boolean,
    private val loadFailure: RewardedAdFailureReason?,
    private val showOutcome: ShowOutcome,
) : RewardedAdDataSource {
    val loadedAdUnitIds = mutableListOf<String>()

    override fun load(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (RewardedAdFailureReason) -> Unit,
    ) {
        loadedAdUnitIds += adUnitId
        loadFailure?.let(onFailed) ?: onLoaded()
    }

    override fun show(
        onEarned: () -> Unit,
        onDismissed: () -> Unit,
        onFailed: () -> Unit,
    ) {
        when (showOutcome) {
            ShowOutcome.EARNED -> {
                onEarned()
                onDismissed()
            }

            ShowOutcome.DISMISSED -> onDismissed()

            ShowOutcome.FAILED -> onFailed()
        }
    }
}
