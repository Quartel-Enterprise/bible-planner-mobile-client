package com.quare.bibleplanner.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    /*
     * Why: only the cold start goes into the startup profile. R8 puts what it lists in the primary
     * dex file, and code a later screen needs would push out code the first frame needs.
     */
    @Test
    fun startup() {
        rule.collect(
            packageName = TARGET_PACKAGE,
            includeInStartupProfile = true,
        ) {
            startAndWaitForPlans()
        }
    }

    @Test
    fun criticalJourneys() {
        rule.collect(packageName = TARGET_PACKAGE) {
            startAndWaitForPlans()
            scrollPlans()
            openFirstDay()
            scrollDay()
            openFirstChapter()
            scrollChapter()
            backToPlans()
            openBooks()
            scrollBooks()
        }
    }
}
