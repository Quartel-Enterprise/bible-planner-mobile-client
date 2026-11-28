package com.quare.bibleplanner.android.feature.login

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.android.designsystem.theme.isDark
import com.quare.bibleplanner.core.model.legal.LegalUrl
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import com.quare.bibleplanner.feature.login.presentation.model.LoginError
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiEvent
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiState
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard

private val progressSize = 20.dp
private val progressStrokeWidth = 2.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LoginContent(
    uiState: LoginUiState,
    onEvent: (LoginUiEvent) -> Unit,
    onProviderClick: (LoginProvider) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val sheetCloseGuard = rememberSheetCloseGuard()
    SheetExitAnimationEffect(
        animateOut = sheetState::hide,
        animateBackIn = sheetState::show,
    )
    ModalBottomSheet(
        onDismissRequest = { sheetCloseGuard.close { onEvent(LoginUiEvent.DismissClick) } },
        sheetState = sheetState,
        sheetGesturesEnabled = !sheetCloseGuard.isClosing,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .blockPointerInput(isBlocked = sheetCloseGuard.isClosing)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            LoginProvidersColumn(
                providers = uiState.enabledProviders,
                loadingProvider = uiState.loadingProvider,
                onProviderClick = onProviderClick,
            )
            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(error.toMessage()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = termsText(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(32.dp))
            TextButton(onClick = { sheetCloseGuard.close { onEvent(LoginUiEvent.NotNowClick) } }) {
                Text(
                    text = stringResource(R.string.login_not_now),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    if (uiState.showGoogleSignInUnavailableDialog) {
        AppAlertDialog(
            title = stringResource(R.string.login_google_sign_in_unavailable_title),
            text = stringResource(R.string.login_google_sign_in_unavailable_message),
            confirmText = stringResource(R.string.login_google_sign_in_unavailable_confirm),
            onConfirm = { onEvent(LoginUiEvent.AddGoogleAccountConfirmClick) },
            dismissText = stringResource(R.string.login_google_sign_in_unavailable_dismiss),
            onDismiss = { onEvent(LoginUiEvent.DismissAddGoogleAccountDialog) },
        )
    }
}

@Composable
private fun LoginProvidersColumn(
    providers: List<LoginProvider>,
    loadingProvider: LoginProvider?,
    onProviderClick: (LoginProvider) -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.isDark()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        providers.forEach { provider ->
            SocialLoginButton(
                text = stringResource(provider.toText()),
                iconRes = provider.toIcon(isDark),
                isLoading = loadingProvider == provider,
                onClick = { onProviderClick(provider) },
            )
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    @DrawableRes iconRes: Int,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(progressSize),
                    strokeWidth = progressStrokeWidth,
                )
            } else {
                Image(
                    modifier = Modifier.align(Alignment.CenterStart),
                    painter = painterResource(iconRes),
                    contentDescription = null,
                )
                Text(text = text)
            }
        }
    }
}

@Composable
private fun termsText(): AnnotatedString {
    val part1 = stringResource(R.string.login_terms_agreement_part_1)
    val termsOfService = stringResource(R.string.login_terms_of_service)
    val part2 = stringResource(R.string.login_terms_agreement_part_2)
    val privacyPolicy = stringResource(R.string.login_privacy_policy)
    val part3 = stringResource(R.string.login_terms_agreement_part_3)
    val linkStyles = TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline))
    return buildAnnotatedString {
        append(part1)
        withLink(
            LinkAnnotation.Url(
                url = LegalUrl.TERMS_OF_SERVICE,
                styles = linkStyles,
            ),
        ) {
            append(termsOfService)
        }
        append(part2)
        withLink(
            LinkAnnotation.Url(
                url = LegalUrl.PRIVACY_POLICY,
                styles = linkStyles,
            ),
        ) {
            append(privacyPolicy)
        }
        append(part3)
    }
}

@StringRes
private fun LoginProvider.toText(): Int = when (this) {
    LoginProvider.GOOGLE -> R.string.login_continue_with_google
    LoginProvider.APPLE -> R.string.login_continue_with_apple
}

@DrawableRes
private fun LoginProvider.toIcon(isDark: Boolean): Int = when (this) {
    LoginProvider.GOOGLE -> if (isDark) R.drawable.login_google_icon_white else R.drawable.login_google_icon_dark
    LoginProvider.APPLE -> if (isDark) R.drawable.login_apple_icon_white else R.drawable.login_apple_icon_dark
}

@StringRes
private fun LoginError.toMessage(): Int = when (this) {
    LoginError.CONNECTION -> R.string.login_error_connection
    LoginError.EMAIL_REQUIRED -> R.string.login_error_email_required
    LoginError.GENERIC -> R.string.login_error_try_again_later
}
