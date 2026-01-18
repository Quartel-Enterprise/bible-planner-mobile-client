package com.quare.bibleplanner.feature.login.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import bibleplanner.feature.login.generated.resources.Res
import bibleplanner.feature.login.generated.resources.not_now
import bibleplanner.feature.login.generated.resources.privacy_policy
import bibleplanner.feature.login.generated.resources.terms_agreement_part_1
import bibleplanner.feature.login.generated.resources.terms_agreement_part_2
import bibleplanner.feature.login.generated.resources.terms_agreement_part_3
import bibleplanner.feature.login.generated.resources.terms_of_service
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import com.quare.bibleplanner.feature.login.presentation.component.LoginProvidersComponent
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiEvent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBottomSheet(
    sheetState: SheetState,
    enableProviders: List<LoginProvider>,
    onEvent: (LoginUiEvent) -> Unit,
    onLoginWithGoogleClick: () -> Unit,
    onLoginWithAppleClick: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onEvent(LoginUiEvent.DismissClick) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoginProvidersComponent(
                providers = enableProviders,
                onLoginWithGoogleClick = onLoginWithGoogleClick,
                onLoginWithAppleClick = onLoginWithAppleClick,
            )

            VerticalSpacer(32)

            val part1 = stringResource(Res.string.terms_agreement_part_1)
            val termsOfService = stringResource(Res.string.terms_of_service)
            val part2 = stringResource(Res.string.terms_agreement_part_2)
            val privacyPolicy = stringResource(Res.string.privacy_policy)
            val part3 = stringResource(Res.string.terms_agreement_part_3)
            val termsText = buildAnnotatedString {
                append(part1)
                withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append(termsOfService)
                }
                append(part2)
                withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append(privacyPolicy)
                }
                append(part3)
            }
            Text(
                text = termsText,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            VerticalSpacer(32)

            TextButton(
                onClick = { onEvent(LoginUiEvent.NotNowClick) },
            ) {
                Text(
                    text = stringResource(Res.string.not_now),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            VerticalSpacer(16)
        }
    }
}
