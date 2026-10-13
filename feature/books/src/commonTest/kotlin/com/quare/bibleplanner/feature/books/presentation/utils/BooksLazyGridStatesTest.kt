package com.quare.bibleplanner.feature.books.presentation.utils

import androidx.compose.foundation.lazy.grid.LazyGridState
import com.quare.bibleplanner.feature.books.fixture.booksUiState
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BookTestamentFilter
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

internal class BooksLazyGridStatesTest {
    private val searchGridState = LazyGridState()
    private val searchListState = LazyGridState()
    private val allTestamentsGridState = LazyGridState()
    private val allTestamentsListState = LazyGridState()
    private val oldTestamentGridState = LazyGridState()
    private val oldTestamentListState = LazyGridState()
    private val newTestamentGridState = LazyGridState()
    private val newTestamentListState = LazyGridState()
    private lateinit var lazyGridStates: BooksLazyGridStates

    @BeforeTest
    fun setUp() {
        lazyGridStates = BooksLazyGridStates(
            searchGridState = searchGridState,
            searchListState = searchListState,
            allTestamentsGridState = allTestamentsGridState,
            allTestamentsListState = allTestamentsListState,
            oldTestamentGridState = oldTestamentGridState,
            oldTestamentListState = oldTestamentListState,
            newTestamentGridState = newTestamentGridState,
            newTestamentListState = newTestamentListState,
        )
    }

    @Test
    fun `GIVEN the books still loading WHEN getting the current state THEN uses the search grid state`() {
        // Given
        val state = BooksUiState.Loading

        // When
        val current = lazyGridStates.getCurrent(state)

        // Then
        assertSame(searchGridState, current)
    }

    @Test
    fun `GIVEN a search in the grid with the old testament WHEN getting the state THEN uses the search grid state`() {
        // Given
        val state = successState(
            testamentFilter = BookTestamentFilter.OldTestament,
            layoutFormat = BookLayoutFormat.Grid,
            shouldShowTestamentToggle = false,
        )

        // When
        val current = lazyGridStates.getCurrent(state)

        // Then
        assertSame(searchGridState, current)
    }

    @Test
    fun `GIVEN a search in the list with the new testament WHEN getting the state THEN uses the search list state`() {
        // Given
        val state = successState(
            testamentFilter = BookTestamentFilter.NewTestament,
            layoutFormat = BookLayoutFormat.List,
            shouldShowTestamentToggle = false,
        )

        // When
        val current = lazyGridStates.getCurrent(state)

        // Then
        assertSame(searchListState, current)
    }

    @Test
    fun `GIVEN all testaments in the grid WHEN getting the current state THEN uses the all testaments grid state`() {
        // Given
        val state = successState(
            testamentFilter = BookTestamentFilter.All,
            layoutFormat = BookLayoutFormat.Grid,
            shouldShowTestamentToggle = true,
        )

        // When
        val current = lazyGridStates.getCurrent(state)

        // Then
        assertSame(allTestamentsGridState, current)
    }

    @Test
    fun `GIVEN all testaments in the list WHEN getting the current state THEN uses the all testaments list state`() {
        // Given
        val state = successState(
            testamentFilter = BookTestamentFilter.All,
            layoutFormat = BookLayoutFormat.List,
            shouldShowTestamentToggle = true,
        )

        // When
        val current = lazyGridStates.getCurrent(state)

        // Then
        assertSame(allTestamentsListState, current)
    }

    @Test
    fun `GIVEN the old testament in the grid WHEN getting the current state THEN uses the old testament grid state`() {
        // Given
        val state = successState(
            testamentFilter = BookTestamentFilter.OldTestament,
            layoutFormat = BookLayoutFormat.Grid,
            shouldShowTestamentToggle = true,
        )

        // When
        val current = lazyGridStates.getCurrent(state)

        // Then
        assertSame(oldTestamentGridState, current)
    }

    @Test
    fun `GIVEN the old testament in the list WHEN getting the current state THEN uses the old testament list state`() {
        // Given
        val state = successState(
            testamentFilter = BookTestamentFilter.OldTestament,
            layoutFormat = BookLayoutFormat.List,
            shouldShowTestamentToggle = true,
        )

        // When
        val current = lazyGridStates.getCurrent(state)

        // Then
        assertSame(oldTestamentListState, current)
    }

    @Test
    fun `GIVEN the new testament in the grid WHEN getting the current state THEN uses the new testament grid state`() {
        // Given
        val state = successState(
            testamentFilter = BookTestamentFilter.NewTestament,
            layoutFormat = BookLayoutFormat.Grid,
            shouldShowTestamentToggle = true,
        )

        // When
        val current = lazyGridStates.getCurrent(state)

        // Then
        assertSame(newTestamentGridState, current)
    }

    @Test
    fun `GIVEN the new testament in the list WHEN getting the current state THEN uses the new testament list state`() {
        // Given
        val state = successState(
            testamentFilter = BookTestamentFilter.NewTestament,
            layoutFormat = BookLayoutFormat.List,
            shouldShowTestamentToggle = true,
        )

        // When
        val current = lazyGridStates.getCurrent(state)

        // Then
        assertSame(newTestamentListState, current)
    }

    private fun successState(
        testamentFilter: BookTestamentFilter,
        layoutFormat: BookLayoutFormat,
        shouldShowTestamentToggle: Boolean,
    ): BooksUiState.Success = booksUiState().copy(
        selectedTestamentFilter = testamentFilter,
        layoutFormat = layoutFormat,
        shouldShowTestamentToggle = shouldShowTestamentToggle,
    )
}
