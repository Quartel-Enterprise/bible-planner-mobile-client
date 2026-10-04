package com.quare.bibleplanner.ui.screenshots.testing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziATFAccessibilityCheckOptions
import com.github.takahirom.roborazzi.RoborazziATFAccessibilityChecker
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.checkRoboAccessibility
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Locale

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [ScreenshotTest.ROBOLECTRIC_SDK], qualifiers = RobolectricDeviceQualifiers.Pixel7)
abstract class ScreenshotTest {
    private val roborazziOptions = RoborazziOptions(
        taskType = when (System.getProperty(MODE_PROPERTY)) {
            RECORD_MODE -> RoborazziTaskType.Record
            VERIFY_MODE -> RoborazziTaskType.Verify
            else -> RoborazziTaskType.None
        },
        recordOptions = RoborazziOptions.RecordOptions(resizeScale = RESIZE_SCALE),
    )

    private val accessibilityCheckOptions = RoborazziATFAccessibilityCheckOptions(
        checker = RoborazziATFAccessibilityChecker(preset = AccessibilityCheckPreset.LATEST),
        failureLevel = RoborazziATFAccessibilityChecker.CheckLevel.Error,
    )

    protected fun snapshot(
        name: String,
        variants: List<ScreenshotVariant> = ScreenshotVariant.themes,
        isLandscape: Boolean = false,
        content: @Composable () -> Unit,
    ) {
        val directory = "$SCREENSHOT_DIRECTORY/${javaClass.simpleName}"
        val defaultLocale = Locale.getDefault()
        try {
            variants.forEach { variant ->
                capture(
                    filePath = "$directory/${name}_${variant.fileSuffix}$SCREENSHOT_EXTENSION",
                    variant = variant,
                    isLandscape = isLandscape,
                    content = content,
                )
            }
        } finally {
            Locale.setDefault(defaultLocale)
        }
    }

    private fun capture(
        filePath: String,
        variant: ScreenshotVariant,
        isLandscape: Boolean,
        content: @Composable () -> Unit,
    ) {
        applyConfiguration(
            variant = variant,
            isLandscape = isLandscape,
        )
        val composeRule = createComposeRule()
        val statement = object : Statement() {
            override fun evaluate() {
                composeRule.setContent {
                    CompositionLocalProvider(LocalTheme provides variant.theme) {
                        AppTheme {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background,
                                content = content,
                            )
                        }
                    }
                }
                composeRule.waitForIdle()
                composeRule.onRoot().captureRoboImage(
                    filePath = filePath,
                    roborazziOptions = roborazziOptions,
                )
                composeRule.checkAccessibility()
            }
        }
        composeRule.apply(statement, Description.createSuiteDescription(filePath)).evaluate()
    }

    private fun ComposeContentTestRule.checkAccessibility() {
        val roots = onAllNodes(isRoot())
        repeat(roots.fetchSemanticsNodes().size) { index ->
            roots[index].checkRoboAccessibility(
                roborazziATFAccessibilityCheckOptions = accessibilityCheckOptions,
                roborazziOptions = roborazziOptions,
            )
        }
    }

    private fun applyConfiguration(
        variant: ScreenshotVariant,
        isLandscape: Boolean,
    ) {
        val locale = Locale.forLanguageTag(variant.languageTag)
        val qualifiers = listOf(
            RobolectricDeviceQualifiers.Pixel7,
            "+${locale.language}-r${locale.country}",
            if (isLandscape) LANDSCAPE_QUALIFIER else PORTRAIT_QUALIFIER,
        )
        qualifiers.forEach(RuntimeEnvironment::setQualifiers)
        RuntimeEnvironment.setFontScale(variant.fontScale)
        Locale.setDefault(locale)
    }

    private companion object {
        const val ROBOLECTRIC_SDK = 35
        const val MODE_PROPERTY = "screenshotTests.mode"
        const val RECORD_MODE = "record"
        const val VERIFY_MODE = "verify"
        const val RESIZE_SCALE = 0.5
        const val PORTRAIT_QUALIFIER = "+port"
        const val LANDSCAPE_QUALIFIER = "+land"
        const val SCREENSHOT_EXTENSION = ".png"
        const val SCREENSHOT_DIRECTORY = "src/androidHostTest/screenshots"
    }
}
