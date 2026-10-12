package com.quare.bibleplanner.ui.testing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlinx.coroutines.test.runTest as runCoroutineTest

class ViewModelTestStore(
    private val testDispatcher: TestDispatcher,
    private val timeout: Duration? = null,
) {
    private val store = ViewModelStore()
    private val viewModelJobs = mutableListOf<Job>()

    fun <VM : ViewModel> create(
        modelClass: KClass<VM>,
        factory: () -> VM,
    ): VM {
        // Why: ViewModelProvider caches by key, so a class-name key would hand a second create() the old instance.
        val viewModel = ViewModelProvider.create(
            store = store,
            factory = viewModelFactory { addInitializer(modelClass) { factory() } },
        )[VIEW_MODEL_KEY_PREFIX + viewModelJobs.size, modelClass]
        viewModelJobs += viewModel.viewModelScope.coroutineContext.job
        return viewModel
    }

    fun clear() {
        store.clear()
    }

    /*
     * Why: a ViewModel whose collaborators run on another dispatcher (flowOn(Dispatchers.Default), Compose Resources)
     * is resumed by a worker thread through Dispatchers.Main. Cleared after runTest, nothing waits for that work, and a
     * worker still inside Dispatchers.Main while @AfterTest calls resetMain() fails the test at random. The test's own
     * failure, cancellation by the timeout included, is caught only to run this teardown first, then rethrown.
     */
    fun runTest(testBody: suspend TestScope.() -> Unit): TestResult {
        val testBodyWithTeardown: suspend TestScope.() -> Unit = {
            val testFailure = runCatching { testBody() }.exceptionOrNull()
            val clearFailure = runCatching(::clear).exceptionOrNull()
            withContext(NonCancellable) { viewModelJobs.joinAll() }
            if (testFailure != null) {
                clearFailure?.let(testFailure::addSuppressed)
                throw testFailure
            }
            clearFailure?.let { throw it }
        }
        return if (timeout == null) {
            runCoroutineTest(
                context = testDispatcher,
                testBody = testBodyWithTeardown,
            )
        } else {
            runCoroutineTest(
                context = testDispatcher,
                timeout = timeout,
                testBody = testBodyWithTeardown,
            )
        }
    }

    private companion object {
        const val VIEW_MODEL_KEY_PREFIX = "view_model_"
    }
}
