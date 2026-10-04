package com.quare.bibleplanner.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.MacrobenchmarkRule

internal fun MacrobenchmarkRule.measureFrames(
    compilationMode: CompilationMode,
    setup: MacrobenchmarkScope.() -> Unit,
    measure: MacrobenchmarkScope.() -> Unit,
) = measureRepeated(
    packageName = TARGET_PACKAGE,
    metrics = listOf(FrameTimingMetric()),
    compilationMode = compilationMode,
    iterations = ITERATIONS,
    setupBlock = setup,
    measureBlock = measure,
)
