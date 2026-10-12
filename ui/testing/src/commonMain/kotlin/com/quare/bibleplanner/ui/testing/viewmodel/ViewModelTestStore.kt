package com.quare.bibleplanner.ui.testing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest as runCoroutineTest

class ViewModelTestStore(
    private val testDispatcher: TestDispatcher,
    private val timeout: Duration = 60.seconds,
) {
    private val store = ViewModelStore()
    private val viewModelJobs = mutableListOf<Job>()

    fun <VM : ViewModel> create(
        modelClass: KClass<VM>,
        factory: () -> VM,
    ): VM {
        val viewModel = ViewModelProvider.create(
            store = store,
            factory = viewModelFactory { addInitializer(modelClass) { factory() } },
        )[modelClass]
        viewModelJobs += viewModel.viewModelScope.coroutineContext.job
        return viewModel
    }

    fun clear() {
        store.clear()
    }

    /*
     * Why: a ViewModel whose collaborators run on another dispatcher (flowOn(Dispatchers.Default), Compose Resources)
     * is resumed by a worker thread through Dispatchers.Main. Cleared after runTest, nothing waits for that work, and a
     * worker still inside Dispatchers.Main while @AfterTest calls resetMain() fails the test at random.
     */
    fun runTest(testBody: suspend TestScope.() -> Unit): TestResult = runCoroutineTest(
        context = testDispatcher,
        timeout = timeout,
    ) {
        try {
            testBody()
        } finally {
            clear()
            viewModelJobs.joinAll()
        }
    }
}
