package com.quare.bibleplanner.feature.paywall.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.sp
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.paywall_subtitle
import bibleplanner.feature.paywall.generated.resources.paywall_title_part_1
import bibleplanner.feature.paywall.generated.resources.paywall_title_part_2
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.ui.testing.AnimationsDisabled
import com.quare.bibleplanner.ui.testing.SharedTransitionTestContent
import com.quare.bibleplanner.ui.testing.setUiTestContent
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
internal class PaywallTopBarUiTest {
    private var isPaywallShown by mutableStateOf(false)

    @Test
    fun `GIVEN animations disabled WHEN opening the paywall THEN shows the title in the top bar`() =
        runComposeUiTest(effectContext = AnimationsDisabled) {
            // Given
            prepareScenario()

            // When
            isPaywallShown = true
            waitForIdle()

            // Then
            val subtitleTop = onNodeWithText(getString(Res.string.paywall_subtitle)).getUnclippedBoundsInRoot().top
            listOf(Res.string.paywall_title_part_1, Res.string.paywall_title_part_2).forEach { titlePart ->
                val titlePartNode = onNodeWithText(getString(titlePart))
                titlePartNode.assertIsDisplayed()
                val titlePartBounds = titlePartNode.getUnclippedBoundsInRoot()
                assertTrue(
                    actual = titlePartBounds.bottom <= subtitleTop,
                    message = "Title part at $titlePartBounds should sit above the subtitle at $subtitleTop",
                )
            }
        }

    private fun ComposeUiTest.prepareScenario() {
        setUiTestContent {
            SharedTransitionTestContent(
                isTargetShown = isPaywallShown,
                source = { animatedContentScope ->
                    BecomeProTitle(
                        sharedTransitionScope = this,
                        animatedVisibilityScope = animatedContentScope,
                        fontSize = 16.sp,
                        titleColor = MaterialTheme.colorScheme.onSurface,
                        proColor = MaterialTheme.colorScheme.onSurface,
                    )
                },
                target = { animatedContentScope ->
                    PaywallTopBar(
                        platform = Platform.Android,
                        sharedTransitionScope = this,
                        animatedVisibilityScope = animatedContentScope,
                        onBackClick = {},
                    )
                },
            )
        }
    }
}
