package com.quare.bibleplanner.feature.profile.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.v2.runComposeUiTest
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.app_language_option
import bibleplanner.feature.profile.generated.resources.bible_version_option
import bibleplanner.feature.profile.generated.resources.delete_account_option
import bibleplanner.feature.profile.generated.resources.edit_profile_action
import bibleplanner.feature.profile.generated.resources.login_card_button
import bibleplanner.feature.profile.generated.resources.login_card_subtitle
import bibleplanner.feature.profile.generated.resources.login_card_title
import bibleplanner.feature.profile.generated.resources.logout_button
import bibleplanner.feature.profile.generated.resources.start_date
import bibleplanner.feature.profile.generated.resources.study_suggestion_enabled_dialog
import bibleplanner.feature.profile.generated.resources.study_suggestion_option
import bibleplanner.feature.profile.generated.resources.theme_option
import bibleplanner.feature.profile.generated.resources.theme_system
import bibleplanner.ui.component.generated.resources.language_english
import com.quare.bibleplanner.feature.profile.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.profile.fixture.SAMPLE_BIBLE_VERSION_NAME
import com.quare.bibleplanner.feature.profile.fixture.profileUiState
import com.quare.bibleplanner.feature.profile.fixture.samplePlanStartDate
import com.quare.bibleplanner.feature.profile.fixture.sampleUserProfile
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import com.quare.bibleplanner.ui.testing.setUiTestContent
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.compose.resources.getString
import bibleplanner.ui.component.generated.resources.Res as ComponentRes

@OptIn(ExperimentalTestApi::class)
internal class ProfileUiTest {
    private val signedInStatus = AccountStatusModel.LoggedIn(profile = sampleUserProfile)
    private lateinit var events: MutableList<ProfileUiEvent>

    @Test
    fun `GIVEN a loaded profile WHEN rendered THEN the preferences rows show their current values`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = profileUiState(accountStatusModel = AccountStatusModel.LoggedOut))
            val planStartDateSubtitle = listOf(
                samplePlanStartDate.day,
                getString(samplePlanStartDate.month.toStringResource()).take(MONTH_ABBREVIATION_LENGTH),
                samplePlanStartDate.year,
            ).joinToString(separator = " ")

            // When
            waitForIdle()

            // Then
            onListNodeWithText(getString(Res.string.theme_system)).assertIsDisplayed()
            onListNodeWithText(getString(ComponentRes.string.language_english)).assertIsDisplayed()
            onListNodeWithText(SAMPLE_BIBLE_VERSION_NAME).assertIsDisplayed()
            onListNodeWithText(planStartDateSubtitle).assertIsDisplayed()
            onListNodeWithText(getString(Res.string.study_suggestion_enabled_dialog)).assertIsDisplayed()
        }

    @Test
    fun `GIVEN a loaded profile WHEN clicking the theme row THEN emits OnItemClick for THEME`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = profileUiState(accountStatusModel = AccountStatusModel.LoggedOut))

        // When
        onListNodeWithText(getString(Res.string.theme_option)).performClick()

        // Then
        assertEquals(
            expected = listOf<ProfileUiEvent>(ProfileUiEvent.OnItemClick(ProfileOptionItemType.THEME)),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a loaded profile WHEN clicking the language row THEN emits OnItemClick for APP_LANGUAGE`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = profileUiState(accountStatusModel = AccountStatusModel.LoggedOut))

            // When
            onListNodeWithText(getString(Res.string.app_language_option)).performClick()

            // Then
            assertEquals(
                expected = listOf<ProfileUiEvent>(ProfileUiEvent.OnItemClick(ProfileOptionItemType.APP_LANGUAGE)),
                actual = events,
            )
        }

    @Test
    fun `GIVEN a loaded profile WHEN clicking the Bible version row THEN emits OnItemClick for BIBLE_VERSION`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = profileUiState(accountStatusModel = AccountStatusModel.LoggedOut))

            // When
            onListNodeWithText(getString(Res.string.bible_version_option)).performClick()

            // Then
            assertEquals(
                expected = listOf<ProfileUiEvent>(ProfileUiEvent.OnItemClick(ProfileOptionItemType.BIBLE_VERSION)),
                actual = events,
            )
        }

    @Test
    fun `GIVEN a loaded profile WHEN clicking plan start date THEN emits OnItemClick for EDIT_PLAN_START_DAY`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = profileUiState(accountStatusModel = AccountStatusModel.LoggedOut))

            // When
            onListNodeWithText(getString(Res.string.start_date)).performClick()

            // Then
            assertEquals(
                expected = listOf<ProfileUiEvent>(
                    ProfileUiEvent.OnItemClick(ProfileOptionItemType.EDIT_PLAN_START_DAY),
                ),
                actual = events,
            )
        }

    @Test
    fun `GIVEN a loaded profile WHEN clicking study suggestion THEN emits OnItemClick for STUDY_SUGGESTION`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = profileUiState(accountStatusModel = AccountStatusModel.LoggedOut))

            // When
            onListNodeWithText(getString(Res.string.study_suggestion_option)).performClick()

            // Then
            assertEquals(
                expected = listOf<ProfileUiEvent>(
                    ProfileUiEvent.OnItemClick(ProfileOptionItemType.STUDY_SUGGESTION),
                ),
                actual = events,
            )
        }

    @Test
    fun `GIVEN a signed out user WHEN rendered THEN the login card invites to sign in`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = profileUiState(accountStatusModel = AccountStatusModel.LoggedOut))

        // When
        waitForIdle()

        // Then
        onListNodeWithText(getString(Res.string.login_card_title)).assertIsDisplayed()
        onListNodeWithText(getString(Res.string.login_card_subtitle)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.logout_button)).assertDoesNotExist()
        onNodeWithText(getString(Res.string.delete_account_option)).assertDoesNotExist()
    }

    @Test
    fun `GIVEN a signed out user WHEN clicking sign in THEN emits OnLoginClick`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = profileUiState(accountStatusModel = AccountStatusModel.LoggedOut))

        // When
        onListNodeWithText(getString(Res.string.login_card_button)).performClick()

        // Then
        assertEquals(expected = listOf<ProfileUiEvent>(ProfileUiEvent.OnLoginClick), actual = events)
    }

    @Test
    fun `GIVEN a signed in user WHEN rendered THEN the account card shows the name and email`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = profileUiState(accountStatusModel = signedInStatus))

        // When
        waitForIdle()

        // Then
        onListNodeWithText(checkNotNull(sampleUserProfile.displayName)).assertIsDisplayed()
        onListNodeWithText(sampleUserProfile.email).assertIsDisplayed()
        onNodeWithText(getString(Res.string.login_card_button)).assertDoesNotExist()
        onListNodeWithText(getString(Res.string.delete_account_option)).assertIsDisplayed()
    }

    @Test
    fun `GIVEN a signed in user WHEN clicking edit profile THEN emits OnEditProfileClick`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = profileUiState(accountStatusModel = signedInStatus))
        val editProfileLabel = getString(Res.string.edit_profile_action)

        // When
        onNode(
            (hasText(editProfileLabel) or hasContentDescription(editProfileLabel)) and hasClickAction(),
        ).performClick()

        // Then
        assertEquals(expected = listOf<ProfileUiEvent>(ProfileUiEvent.OnEditProfileClick), actual = events)
    }

    @Test
    fun `GIVEN a signed in user WHEN clicking sign out THEN emits OnLogoutClick`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = profileUiState(accountStatusModel = signedInStatus))

        // When
        onListNodeWithText(getString(Res.string.logout_button)).performClick()

        // Then
        assertEquals(expected = listOf<ProfileUiEvent>(ProfileUiEvent.OnLogoutClick), actual = events)
    }

    private fun ComposeUiTest.onListNodeWithText(text: String): SemanticsNodeInteraction {
        val matcher = hasText(text)
        onNode(hasScrollToIndexAction()).performScrollToNode(matcher)
        return onNode(matcher)
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    private fun ComposeUiTest.prepareScenario(uiState: ProfileUiState) {
        events = mutableListOf()
        setUiTestContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = Unit) {
                    ProfileScreen(
                        state = uiState,
                        onEvent = { event -> events += event },
                        becomeProTitleContent = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@AnimatedContent,
                    )
                }
            }
        }
    }

    private companion object {
        const val MONTH_ABBREVIATION_LENGTH = 3
    }
}
