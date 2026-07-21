package com.quare.bibleplanner.feature.editprofile.presentation

import com.quare.bibleplanner.core.model.route.EditNameNavRoute
import com.quare.bibleplanner.core.model.route.EditPhotoSourceNavRoute
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditProfileUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditProfileUiEvent
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.EditProfileViewModel
import kotlinx.coroutines.Dispatchers
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

class EditProfileViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `replaces the sheet with the name dialog`() = runTest {
        // When
        val actions = actionsAfter(EditProfileUiEvent.OnChangeNameClick)

        // Then
        assertEquals(listOf(EditProfileUiAction(EditNameNavRoute)), actions)
    }

    @Test
    fun `replaces the sheet with the photo sheet`() = runTest {
        // When
        val actions = actionsAfter(EditProfileUiEvent.OnChangeImageClick)

        // Then
        assertEquals(listOf(EditProfileUiAction(EditPhotoSourceNavRoute)), actions)
    }

    private suspend fun TestScope.actionsAfter(event: EditProfileUiEvent): List<EditProfileUiAction> {
        val viewModel = EditProfileViewModel(trackEvent = { _, _ -> })
        val actions = mutableListOf<EditProfileUiAction>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiAction.collect(actions::add)
        }
        viewModel.onEvent(event)
        advanceUntilIdle()
        job.cancel()
        return actions
    }
}
