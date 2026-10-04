package com.quare.bibleplanner.feature.profile.screenshots

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.profile.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.profile.fixture.profileUiState
import com.quare.bibleplanner.feature.profile.fixture.sampleUserProfile
import com.quare.bibleplanner.feature.profile.presentation.ProfileScreen
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import com.quare.bibleplanner.ui.screenshots.testing.ScreenshotTest
import com.quare.bibleplanner.ui.screenshots.testing.ScreenshotVariant
import org.junit.Test

internal class ProfileScreenshotTest : ScreenshotTest() {
    @Test
    fun loggedOut() {
        snapshot(
            name = "logged_out",
            variants = ScreenshotVariant.all,
        ) {
            ProfileContent(uiState = profileUiState(accountStatusModel = AccountStatusModel.LoggedOut))
        }
    }

    @Test
    fun loggedIn() {
        snapshot(
            name = "logged_in",
            variants = ScreenshotVariant.all,
        ) {
            ProfileContent(
                uiState = profileUiState(accountStatusModel = AccountStatusModel.LoggedIn(profile = sampleUserProfile)),
            )
        }
    }

    @Composable
    private fun ProfileContent(uiState: ProfileUiState) {
        SharedTransitionLayout {
            AnimatedContent(targetState = Unit) {
                ProfileScreen(
                    state = uiState,
                    onEvent = {},
                    becomeProTitleContent = {},
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@AnimatedContent,
                )
            }
        }
    }
}
