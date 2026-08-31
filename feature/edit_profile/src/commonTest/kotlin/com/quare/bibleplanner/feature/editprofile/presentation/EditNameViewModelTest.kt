package com.quare.bibleplanner.feature.editprofile.presentation

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.profile.domain.usecase.ObserveUserProfile
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditNameUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditNameUiEvent
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.EditNameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditNameViewModelTest {
    private val navigator = Navigator()
    private val commands = mutableListOf<NavigationCommand>()
    private lateinit var updatedNames: MutableList<String>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        updatedNames = mutableListOf()
        commands.clear()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saves the trimmed name and closes the dialog`() = runTest {
        // When
        val actions = actionsAfter(EditNameUiEvent.OnSaveClick("  Outro Nome  "))

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(listOf("Outro Nome"), updatedNames)
    }

    @Test
    fun `dismisses without saving when the name did not change`() = runTest {
        // When
        val actions = actionsAfter(EditNameUiEvent.OnSaveClick(CURRENT_NAME))

        // Then
        assertTrue(actions.isEmpty())
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertTrue(updatedNames.isEmpty())
    }

    @Test
    fun `dismisses without saving when the name only differs by surrounding spaces`() = runTest {
        // When
        val actions = actionsAfter(EditNameUiEvent.OnSaveClick("  $CURRENT_NAME  "))

        // Then
        assertTrue(actions.isEmpty())
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertTrue(updatedNames.isEmpty())
    }

    @Test
    fun `does not save a blank name`() = runTest {
        // When
        val actions = actionsAfter(EditNameUiEvent.OnSaveClick("   "))

        // Then
        assertTrue(actions.isEmpty())
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertTrue(updatedNames.isEmpty())
    }

    private suspend fun TestScope.actionsAfter(event: EditNameUiEvent): List<EditNameUiAction> {
        val viewModel = EditNameViewModel(
            observeUserProfile = ObserveUserProfile { flowOf(profile()) },
            updateDisplayName = { name -> updatedNames.add(name) },
            navigator = navigator,
            trackEvent = { _, _ -> },
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { navigator.commands.collect(commands::add) }
        val actions = mutableListOf<EditNameUiAction>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiAction.collect(actions::add)
        }
        viewModel.onEvent(event)
        advanceUntilIdle()
        job.cancel()
        return actions
    }

    private fun profile() = UserProfile(
        userId = "user-id",
        displayName = CURRENT_NAME,
        email = "user@example.com",
        avatar = AvatarSource.None,
        hasVisiblePhoto = false,
        hasProviderPhoto = true,
        isUsingProviderPhoto = true,
        provider = "google",
    )

    private companion object {
        const val CURRENT_NAME = "Current Name"
    }
}
