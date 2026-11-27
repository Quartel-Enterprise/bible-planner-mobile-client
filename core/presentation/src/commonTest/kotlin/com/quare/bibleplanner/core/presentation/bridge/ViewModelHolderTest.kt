package com.quare.bibleplanner.core.presentation.bridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class ViewModelHolderTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val createdViewModels = mutableListOf<FakeViewModel>()
    private lateinit var holder: ViewModelHolder<FakeViewModel>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        holder = ViewModelHolder(FakeViewModel::class) {
            FakeViewModel().also { createdViewModels += it }
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a holder WHEN reading the view model THEN returns the instance the factory created`() {
        // When
        val viewModel = holder.viewModel

        // Then
        assertSame(
            expected = createdViewModels.single(),
            actual = viewModel,
        )
    }

    @Test
    fun `GIVEN work launched in the view model scope WHEN clearing THEN cancels that work`() {
        // Given
        val work = holder.viewModel.launchEndlessWork()

        // When
        holder.clear()

        // Then
        assertTrue(work.isCancelled)
    }

    @Test
    fun `GIVEN a holder WHEN clearing THEN calls onCleared of the view model`() {
        // Given
        val viewModel = holder.viewModel

        // When
        holder.clear()

        // Then
        assertEquals(
            expected = 1,
            actual = viewModel.clearedCount,
        )
    }

    @Test
    fun `GIVEN a cleared holder WHEN reading the view model THEN fails`() {
        // Given
        holder.clear()

        // When
        val result = runCatching { holder.viewModel }

        // Then
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `GIVEN two holders WHEN clearing one THEN the view model of the other is not cleared`() {
        // Given
        val otherHolder = ViewModelHolder(FakeViewModel::class) { FakeViewModel() }

        // When
        otherHolder.clear()

        // Then
        assertEquals(
            expected = 0,
            actual = holder.viewModel.clearedCount,
        )
    }

    @Test
    fun `GIVEN a cleared holder WHEN clearing again THEN does not call onCleared a second time`() {
        // Given
        val viewModel = holder.viewModel
        holder.clear()

        // When
        holder.clear()

        // Then
        assertEquals(
            expected = 1,
            actual = viewModel.clearedCount,
        )
    }
}

private class FakeViewModel : ViewModel() {
    var clearedCount = 0
        private set

    fun launchEndlessWork(): Job = viewModelScope.launch { awaitCancellation() }

    override fun onCleared() {
        clearedCount++
    }
}
