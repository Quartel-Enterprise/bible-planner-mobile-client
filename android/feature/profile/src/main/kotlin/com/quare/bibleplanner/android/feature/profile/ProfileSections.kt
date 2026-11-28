package com.quare.bibleplanner.android.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import software.quare.loadable.isLoading

private const val LEGAL_SEPARATOR = "•"
private val logoutButtonMinHeight = 44.dp

@Composable
internal fun ProfilePreferencesSection(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    ProfileMenuSection(title = stringResource(R.string.profile_preferences)) {
        ProfileMenuItem(
            title = stringResource(R.string.profile_theme_option),
            icon = rememberVectorPainter(Icons.Default.Palette),
            subtitle = themeSubtitle(state),
            isSubtitleLoading = state.theme.isLoading(),
            onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.THEME)) },
        )
        HorizontalDivider()
        ProfileMenuItem(
            title = stringResource(R.string.profile_app_language_option),
            icon = rememberVectorPainter(Icons.Default.Translate),
            subtitle = languageSubtitle(state),
            isSubtitleLoading = state.selectedLanguage.isLoading(),
            onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.APP_LANGUAGE)) },
        )
        HorizontalDivider()
        ProfileMenuItem(
            title = stringResource(R.string.profile_bible_version_option),
            icon = rememberVectorPainter(Icons.AutoMirrored.Filled.MenuBook),
            subtitle = toBibleVersionSubtitle(state),
            isSubtitleLoading = state.bibleVersionName.isLoading(),
            onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.BIBLE_VERSION)) },
        )
        HorizontalDivider()
        ProfileMenuItem(
            title = stringResource(R.string.profile_start_date),
            icon = rememberVectorPainter(Icons.Default.EditCalendar),
            subtitle = planStartDateSubtitle(state),
            isSubtitleLoading = state.planStartDate.isLoading(),
            onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.EDIT_PLAN_START_DAY)) },
        )
        HorizontalDivider()
        ProfileMenuItem(
            title = stringResource(R.string.profile_study_suggestion_option),
            icon = rememberVectorPainter(Icons.Default.AutoAwesome),
            subtitle = studySuggestionSubtitle(state),
            isSubtitleLoading = state.studySuggestion.isLoading(),
            onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.STUDY_SUGGESTION)) },
        )
    }
}

@Composable
internal fun ProfileSupportSection(onEvent: (ProfileUiEvent) -> Unit) {
    ProfileMenuSection(title = stringResource(R.string.profile_support_comments_section)) {
        ProfileMenuItem(
            title = stringResource(R.string.profile_contact_support_option),
            icon = rememberVectorPainter(Icons.Default.SupportAgent),
            onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.CONTACT_SUPPORT)) },
        )
        HorizontalDivider()
        ProfileMenuItem(
            title = stringResource(R.string.profile_rate_app_option),
            icon = rememberVectorPainter(Icons.Default.RateReview),
            onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.RATE_APP)) },
            trailingContent = { OpenInNewIcon() },
        )
    }
}

@Composable
internal fun ProfileAppSection(
    state: ProfileUiState,
    transitionScopes: ProfileTransitionScopes?,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    ProfileMenuSection(title = stringResource(R.string.profile_app_section)) {
        if (state.isUpdateRowVisible) {
            ProfileMenuItem(
                title = stringResource(R.string.profile_check_for_updates_option),
                icon = rememberVectorPainter(Icons.Default.Update),
                subtitle = if (state.isCheckingForUpdate) {
                    stringResource(R.string.profile_check_for_updates_checking)
                } else {
                    stringResource(R.string.profile_check_for_updates_subtitle, state.appVersion)
                },
                onClick = {
                    if (!state.isCheckingForUpdate) {
                        onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.CHECK_FOR_UPDATE))
                    }
                },
                trailingContent = if (state.isCheckingForUpdate) {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                } else {
                    null
                },
            )
            HorizontalDivider()
        }
        ProfileMenuItem(
            title = stringResource(R.string.profile_release_notes_option),
            icon = rememberVectorPainter(Icons.Default.Description),
            subtitle = stringResource(R.string.profile_release_notes_subtitle),
            headlineModifier = Modifier.sharedElementOf(transitionScopes, RELEASE_NOTES_KEY),
            onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.RELEASE_NOTES)) },
        )
    }
}

@Composable
internal fun ProfileSocialSection(onEvent: (ProfileUiEvent) -> Unit) {
    ProfileMenuSection(title = stringResource(R.string.profile_social_section)) {
        ProfileMenuItem(
            title = stringResource(R.string.profile_instagram),
            icon = painterResource(R.drawable.profile_ic_instagram),
            onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.INSTAGRAM)) },
            trailingContent = { OpenInNewIcon() },
        )
    }
}

@Composable
internal fun ProfileAccountDataSection(
    isLoggedIn: Boolean,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    ProfileMenuSection(title = stringResource(R.string.profile_account_data_section)) {
        ProfileMenuItem(
            title = stringResource(R.string.profile_delete_progress_option),
            icon = rememberVectorPainter(Icons.Default.DeleteSweep),
            subtitle = stringResource(R.string.profile_delete_progress_option_subtitle),
            onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.DELETE_PROGRESS)) },
        )
        if (isLoggedIn) {
            HorizontalDivider()
            ProfileMenuItem(
                title = stringResource(R.string.profile_delete_account_option),
                icon = rememberVectorPainter(Icons.Default.PersonRemove),
                subtitle = stringResource(R.string.profile_delete_account_option_subtitle),
                onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.DELETE_ACCOUNT)) },
                iconColor = MaterialTheme.colorScheme.error,
                headlineColor = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
internal fun ProfileLogoutButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = logoutButtonMinHeight),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = stringResource(R.string.profile_logout_button))
    }
}

@Composable
internal fun ProfileFooterColumn(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.PRIVACY_POLICY)) }) {
                Text(text = stringResource(R.string.profile_privacy_policy))
            }
            Text(text = LEGAL_SEPARATOR)
            TextButton(onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.TERMS)) }) {
                Text(text = stringResource(R.string.profile_terms_of_service))
            }
            if (state.isPrivacyOptionsVisible) {
                Text(text = LEGAL_SEPARATOR)
                TextButton(onClick = { onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.PRIVACY_OPTIONS)) }) {
                    Text(text = stringResource(R.string.profile_privacy_options))
                }
            }
        }
        Text(
            text = stringResource(R.string.profile_app_version_disclaimer, state.appVersion),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun OpenInNewIcon() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
        contentDescription = null,
    )
}
