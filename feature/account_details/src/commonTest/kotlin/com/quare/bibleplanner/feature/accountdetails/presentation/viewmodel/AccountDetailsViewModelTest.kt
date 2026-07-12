package com.quare.bibleplanner.feature.accountdetails.presentation.viewmodel

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor
import com.quare.bibleplanner.core.devices.domain.model.DeviceModel
import com.quare.bibleplanner.core.devices.domain.usecase.ObserveDevices
import com.quare.bibleplanner.core.devices.domain.usecase.SignOutDevice
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.core.model.route.RenameDeviceNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.user.domain.model.UserModel
import com.quare.bibleplanner.core.user.domain.usecase.ObserveCurrentUser
import com.quare.bibleplanner.feature.accountdetails.presentation.mapper.DeviceUiModelMapper
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiAction
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiEvent
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiState
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountInfo
import com.quare.bibleplanner.feature.accountdetails.presentation.model.DeviceUiModel
import com.quare.bibleplanner.feature.accountdetails.presentation.model.LoginMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
internal class AccountDetailsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mapper = DeviceUiModelMapper()
    private val userFlow = MutableStateFlow<UserModel?>(null)
    private val devicesFlow = MutableStateFlow<List<DeviceModel>>(emptyList())
    private val signedOutIds = mutableListOf<String>()
    private val trackedEvents = mutableListOf<String>()
    private lateinit var viewModel: AccountDetailsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AccountDetailsViewModel(
            observeCurrentUser = { userFlow },
            observeDevices = { devicesFlow },
            deviceUiModelMapper = mapper,
            signOutDevice = { deviceRowId ->
                signedOutIds += deviceRowId
                Result.success(Unit)
            },
            trackEvent = { name, _ -> trackedEvents += name },
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a logged-in user and devices WHEN observing THEN maps account info and devices`() =
        runTest(testDispatcher) {
            // Given
            userFlow.value = user(provider = "apple")
            devicesFlow.value = listOf(device(id = "row-1", isCurrent = true))

            // When
            val states = collectStates()
            runCurrent()

            // Then
            val state = states.last()
            val accountInfo = assertIs<Loadable.Loaded<AccountInfo?>>(state.accountInfo).value
            assertEquals(LoginMethod.APPLE, accountInfo?.loginMethod)
            val devices = assertIs<Loadable.Loaded<List<DeviceUiModel>>>(state.devices).value
            assertEquals(1, devices.size)
        }

    @Test
    fun `GIVEN collapsed devices WHEN toggling THEN expands`() = runTest(testDispatcher) {
        // Given
        val states = collectStates()

        // When
        viewModel.onEvent(AccountDetailsUiEvent.OnToggleDevices)
        runCurrent()

        // Then
        assertTrue(states.last().isDevicesExpanded)
        assertTrue(trackedEvents.contains(AnalyticsEventNames.CONNECTED_DEVICES_TOGGLED))
    }

    @Test
    fun `GIVEN a rename click WHEN handling THEN navigates to the rename destination`() = runTest(testDispatcher) {
        // Given
        val device = mapper.map(device(id = "row-9", isCurrent = false))
        val actions = collectActions()

        // When
        viewModel.onEvent(AccountDetailsUiEvent.OnRenameDeviceClick(device))
        runCurrent()

        // Then
        assertEquals(
            AccountDetailsUiAction.NavigateToRoute(
                RenameDeviceNavRoute(deviceRowId = "row-9", currentName = device.name),
            ),
            actions.last(),
        )
    }

    @Test
    fun `GIVEN the current device WHEN signing out THEN routes to logout without revoking`() = runTest(testDispatcher) {
        // Given
        val current = mapper.map(device(id = "row-1", isCurrent = true))
        val actions = collectActions()

        // When
        viewModel.onEvent(AccountDetailsUiEvent.OnSignOutDeviceClick(current))
        runCurrent()

        // Then
        assertEquals(AccountDetailsUiAction.ReplaceWithRoute(LogoutNavRoute), actions.last())
        assertTrue(signedOutIds.isEmpty())
    }

    @Test
    fun `GIVEN another device WHEN signing out THEN revokes that device`() = runTest(testDispatcher) {
        // Given
        val other = mapper.map(device(id = "row-7", isCurrent = false))

        // When
        viewModel.onEvent(AccountDetailsUiEvent.OnSignOutDeviceClick(other))
        runCurrent()

        // Then
        assertEquals(listOf("row-7"), signedOutIds)
        assertTrue(trackedEvents.contains(AnalyticsEventNames.DEVICE_SIGNED_OUT))
    }

    @Test
    fun `GIVEN a device WHEN signing out is in progress THEN it is marked as signing out`() = runTest(testDispatcher) {
        // Given a device still present in the list
        devicesFlow.value = listOf(device(id = "row-7", isCurrent = false))
        val states = collectStates()
        runCurrent()
        val other = mapper.map(device(id = "row-7", isCurrent = false))

        // When
        viewModel.onEvent(AccountDetailsUiEvent.OnSignOutDeviceClick(other))
        runCurrent()

        // Then
        val devices = assertIs<Loadable.Loaded<List<DeviceUiModel>>>(states.last().devices).value
        assertTrue(devices.single { it.id == "row-7" }.isSigningOut)
    }

    private fun TestScope.collectStates(): List<AccountDetailsUiState> {
        val states = mutableListOf<AccountDetailsUiState>()
        backgroundScope.launch { viewModel.uiState.collect { states += it } }
        return states
    }

    private fun TestScope.collectActions(): List<AccountDetailsUiAction> {
        val actions = mutableListOf<AccountDetailsUiAction>()
        backgroundScope.launch { viewModel.uiAction.collect { actions += it } }
        return actions
    }

    private fun user(provider: String) = UserModel(
        id = "user-1",
        name = "Pierre",
        email = "pierre@example.com",
        photo = null,
        provider = provider,
        lastSignInAt = Instant.parse("2026-07-11T08:24:00Z"),
        createdAt = Instant.parse("2026-06-22T10:00:00Z"),
    )

    private fun device(
        id: String,
        isCurrent: Boolean,
    ) = DeviceModel(
        id = id,
        deviceId = "device-$id",
        name = "Device $id",
        formFactor = DeviceFormFactor.PHONE,
        locationCity = "São Paulo",
        locationCountry = "BR",
        lastActiveAt = Instant.parse("2026-07-11T12:00:00Z"),
        isCurrentDevice = isCurrent,
    )
}
