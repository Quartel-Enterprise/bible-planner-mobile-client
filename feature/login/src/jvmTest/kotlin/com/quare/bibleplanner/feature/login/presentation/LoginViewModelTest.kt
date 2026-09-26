package com.quare.bibleplanner.feature.login.presentation

import bibleplanner.feature.login.generated.resources.Res
import bibleplanner.feature.login.generated.resources.login_result_error
import bibleplanner.feature.login.generated.resources.login_result_success
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import com.quare.bibleplanner.feature.login.presentation.factory.LoginUiStateFactory
import com.quare.bibleplanner.feature.login.presentation.mapper.ThrowableToLoginErrorMapper
import com.quare.bibleplanner.feature.login.presentation.model.LoginError
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiAction
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiEvent
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiState
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.NativeSignInState
import io.github.jan.supabase.compose.auth.composable.SignInResultData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class LoginViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val beforeSheetAnimationEnds = 249.milliseconds
    private val supabaseClient = createTestSupabaseClient()
    private val nativeSignInState = NativeSignInState(supabaseClient.defaultSerializer)
    private val credentialUnavailableException = IllegalStateException("no credential")
    private lateinit var viewModel: LoginViewModel
    private lateinit var authenticatedUserId: MutableStateFlow<String?>
    private lateinit var actions: List<LoginUiAction>
    private lateinit var commands: List<NavigationCommand>
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>
    private var addGoogleAccountLaunches = 0

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN the login sheet WHEN opening THEN offers every provider without loading`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val state = viewModel.state.value

        // Then
        assertEquals(
            LoginUiState(
                enabledProviders = listOf(LoginProvider.GOOGLE, LoginProvider.APPLE),
                loadingProvider = null,
                error = null,
                showGoogleSignInUnavailableDialog = false,
            ),
            state,
        )
    }

    @Test
    fun `GIVEN the user signs in WHEN the session becomes authenticated THEN closes the sheet then navigates back`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            authenticatedUserId.value = "user-1"
            advanceTimeBy(beforeSheetAnimationEnds)
            val commandsBeforeAnimationEnds = commands.toList()
            advanceUntilIdle()

            // Then
            assertEquals(listOf<LoginUiAction>(LoginUiAction.CloseBottomSheet), actions)
            assertTrue(commandsBeforeAnimationEnds.isEmpty())
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        }

    @Test
    fun `GIVEN the sheet is open WHEN dismissing it THEN navigates back and tracks the dismissal`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(LoginUiEvent.DismissClick)

            // Then
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
            assertEquals(
                AnalyticsEventNames.LOGIN_SHEET_DISMISSED to
                    mapOf<String, Any>(AnalyticsParams.METHOD to "close_button"),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN the sheet is open WHEN choosing not now THEN closes the sheet before navigating back`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(LoginUiEvent.NotNowClick)
            advanceUntilIdle()

            // Then
            assertEquals(listOf<LoginUiAction>(LoginUiAction.CloseBottomSheet), actions)
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
            assertEquals(
                AnalyticsEventNames.LOGIN_SHEET_DISMISSED to mapOf<String, Any>(AnalyticsParams.METHOD to "not_now"),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN the sign in flow starts WHEN clicking a provider THEN shows it loading and tracks the start`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(LoginUiEvent.SocialLoginClick(LoginProvider.APPLE, nativeSignInState))

            // Then
            assertEquals(LoginProvider.APPLE, viewModel.state.value.loadingProvider)
            assertNull(viewModel.state.value.error)
            assertEquals(
                AnalyticsEventNames.LOGIN_STARTED to mapOf<String, Any>(AnalyticsParams.METHOD to "apple"),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN the sign in flow cannot start WHEN clicking a provider THEN stops loading and shows the error`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(signInResult = Result.failure(IllegalStateException("boom")))

            // When
            viewModel.onEvent(LoginUiEvent.SocialLoginClick(LoginProvider.GOOGLE, nativeSignInState))

            // Then
            assertNull(viewModel.state.value.loadingProvider)
            assertEquals(LoginError.GENERIC, viewModel.state.value.error)
            assertEquals(
                AnalyticsEventNames.LOGIN_FAILED to mapOf<String, Any>(
                    AnalyticsParams.METHOD to "google",
                    AnalyticsParams.REASON to "generic",
                ),
                trackedEvents.last(),
            )
        }

    @Test
    fun `GIVEN a new account WHEN the native sign in succeeds THEN tracks a sign up and notifies the success`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isNewAccount = true)

            // When
            viewModel.onEvent(
                LoginUiEvent.SocialAuthResult(
                    provider = LoginProvider.GOOGLE,
                    result = NativeSignInResult.Success(SignInResultData.Google()),
                ),
            )

            // Then
            assertEquals(
                AnalyticsEventNames.SIGN_UP to mapOf<String, Any>(AnalyticsParams.METHOD to "google"),
                trackedEvents.single(),
            )
            assertEquals(
                listOf<LoginUiAction>(LoginUiAction.NotifyLoginResult(Res.string.login_result_success)),
                actions,
            )
        }

    @Test
    fun `GIVEN an existing account WHEN the native sign in succeeds THEN tracks a login and keeps loading`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isNewAccount = false)
            viewModel.onEvent(LoginUiEvent.SocialLoginClick(LoginProvider.APPLE, nativeSignInState))

            // When
            viewModel.onEvent(
                LoginUiEvent.SocialAuthResult(
                    provider = LoginProvider.APPLE,
                    result = NativeSignInResult.Success(SignInResultData.Apple()),
                ),
            )

            // Then
            assertEquals(
                AnalyticsEventNames.LOGIN to mapOf<String, Any>(AnalyticsParams.METHOD to "apple"),
                trackedEvents.last(),
            )
            assertEquals(LoginProvider.APPLE, viewModel.state.value.loadingProvider)
        }

    @Test
    fun `GIVEN a provider loading WHEN the user closes the native sign in THEN stops loading without any message`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            viewModel.onEvent(LoginUiEvent.SocialLoginClick(LoginProvider.GOOGLE, nativeSignInState))

            // When
            viewModel.onEvent(
                LoginUiEvent.SocialAuthResult(
                    provider = LoginProvider.GOOGLE,
                    result = NativeSignInResult.ClosedByUser,
                ),
            )

            // Then
            assertNull(viewModel.state.value.loadingProvider)
            assertNull(viewModel.state.value.error)
            assertTrue(actions.isEmpty())
            assertEquals(
                AnalyticsEventNames.LOGIN_CANCELLED to mapOf<String, Any>(AnalyticsParams.METHOD to "google"),
                trackedEvents.last(),
            )
        }

    @Test
    fun `GIVEN no network WHEN the native sign in fails THEN shows a connection error`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(
            LoginUiEvent.SocialAuthResult(
                provider = LoginProvider.GOOGLE,
                result = NativeSignInResult.NetworkError("offline"),
            ),
        )

        // Then
        assertEquals(LoginError.CONNECTION, viewModel.state.value.error)
        assertFalse(viewModel.state.value.showGoogleSignInUnavailableDialog)
        assertEquals(listOf<LoginUiAction>(LoginUiAction.NotifyLoginResult(Res.string.login_result_error)), actions)
        assertEquals(
            AnalyticsEventNames.LOGIN_FAILED to mapOf<String, Any>(
                AnalyticsParams.METHOD to "google",
                AnalyticsParams.REASON to "connection",
            ),
            trackedEvents.single(),
        )
    }

    @Test
    fun `GIVEN a missing provider email WHEN the native sign in fails THEN asks for the email`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(
                LoginUiEvent.SocialAuthResult(
                    provider = LoginProvider.APPLE,
                    result = NativeSignInResult.Error(
                        message = "failed",
                        exception = IllegalStateException("Error getting user email from external provider"),
                    ),
                ),
            )

            // Then
            assertEquals(LoginError.EMAIL_REQUIRED, viewModel.state.value.error)
            assertEquals(
                AnalyticsEventNames.LOGIN_FAILED to mapOf<String, Any>(
                    AnalyticsParams.METHOD to "apple",
                    AnalyticsParams.REASON to "email_required",
                ),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN no google credential on the device WHEN the native sign in fails THEN offers to add a google account`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(
                LoginUiEvent.SocialAuthResult(
                    provider = LoginProvider.GOOGLE,
                    result = NativeSignInResult.Error(
                        message = "failed",
                        exception = credentialUnavailableException,
                    ),
                ),
            )

            // Then
            assertTrue(viewModel.state.value.showGoogleSignInUnavailableDialog)
            assertNull(viewModel.state.value.error)
            assertTrue(actions.isEmpty())
            assertEquals(
                AnalyticsEventNames.LOGIN_FAILED to mapOf<String, Any>(
                    AnalyticsParams.METHOD to "google",
                    AnalyticsParams.REASON to "google_unavailable",
                ),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN the add google account dialog WHEN confirming THEN opens the add account flow and hides the dialog`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            showGoogleSignInUnavailableDialog()

            // When
            viewModel.onEvent(LoginUiEvent.AddGoogleAccountConfirmClick)

            // Then
            assertEquals(1, addGoogleAccountLaunches)
            assertFalse(viewModel.state.value.showGoogleSignInUnavailableDialog)
            assertEquals(AnalyticsEventNames.GOOGLE_ACCOUNT_ADD_CONFIRMED to emptyMap(), trackedEvents.last())
        }

    @Test
    fun `GIVEN the add google account dialog WHEN dismissing it THEN hides it without opening the add account flow`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            showGoogleSignInUnavailableDialog()

            // When
            viewModel.onEvent(LoginUiEvent.DismissAddGoogleAccountDialog)

            // Then
            assertEquals(0, addGoogleAccountLaunches)
            assertFalse(viewModel.state.value.showGoogleSignInUnavailableDialog)
            assertEquals(AnalyticsEventNames.GOOGLE_ACCOUNT_ADD_DECLINED to emptyMap(), trackedEvents.last())
        }

    private fun showGoogleSignInUnavailableDialog() {
        viewModel.onEvent(
            LoginUiEvent.SocialAuthResult(
                provider = LoginProvider.GOOGLE,
                result = NativeSignInResult.Error(
                    message = "failed",
                    exception = credentialUnavailableException,
                ),
            ),
        )
    }

    private fun TestScope.prepareScenario(
        signInResult: Result<Unit> = Result.success(Unit),
        isNewAccount: Boolean = false,
    ) {
        val navigator = Navigator()
        val recordedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        trackedEvents = recordedEvents
        addGoogleAccountLaunches = 0
        authenticatedUserId = MutableStateFlow(null)
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        viewModel = LoginViewModel(
            signInStarter = { _, _ -> signInResult },
            throwableToLoginErrorMapper = ThrowableToLoginErrorMapper(),
            isGoogleCredentialUnavailable = { error -> error === credentialUnavailableException },
            addGoogleAccountLauncher = { addGoogleAccountLaunches++ },
            isNewAccount = { isNewAccount },
            navigator = navigator,
            supabaseClient = supabaseClient,
            observeAuthenticatedUserId = { authenticatedUserId },
            uiStateFactory = LoginUiStateFactory(Platform.Android),
            trackEvent = { name, params -> recordedEvents += name to params },
        )
        actions = mutableListOf<LoginUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }
}
