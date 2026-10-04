package com.quare.bibleplanner.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
 * Why: each setup kills the app itself, so every iteration scrolls a list drawn for the first
 * time. StartupMode.COLD would kill it after the setup, and the measured block would find nothing.
 */
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun plansWithoutProfile() {
        plans(CompilationMode.None())
    }

    @Test
    fun plansWithProfile() {
        plans(CompilationMode.Partial(BaselineProfileMode.Require))
    }

    @Test
    fun booksWithoutProfile() {
        books(CompilationMode.None())
    }

    @Test
    fun booksWithProfile() {
        books(CompilationMode.Partial(BaselineProfileMode.Require))
    }

    private fun plans(compilationMode: CompilationMode) {
        rule.measureFrames(
            compilationMode = compilationMode,
            setup = {
                killProcess()
                startAndWaitForPlans()
            },
            measure = { scrollPlans() },
        )
    }

    private fun books(compilationMode: CompilationMode) {
        rule.measureFrames(
            compilationMode = compilationMode,
            setup = {
                killProcess()
                startAndWaitForPlans()
            },
            measure = {
                openBooks()
                scrollBooks()
            },
        )
    }
}
