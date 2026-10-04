package com.quare.bibleplanner.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReadingBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun dayWithoutProfile() = day(CompilationMode.None())

    @Test
    fun dayWithProfile() = day(CompilationMode.Partial(BaselineProfileMode.Require))

    @Test
    fun chapterWithoutProfile() = chapter(CompilationMode.None())

    @Test
    fun chapterWithProfile() = chapter(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun day(compilationMode: CompilationMode) = rule.measureFrames(
        compilationMode = compilationMode,
        setup = {
            killProcess()
            startAndWaitForPlans()
        },
        measure = {
            openFirstDay()
            scrollDay()
        },
    )

    private fun chapter(compilationMode: CompilationMode) = rule.measureFrames(
        compilationMode = compilationMode,
        setup = {
            killProcess()
            startAndWaitForPlans()
            openFirstDay()
        },
        measure = {
            openFirstChapter()
            scrollChapter()
        },
    )
}
