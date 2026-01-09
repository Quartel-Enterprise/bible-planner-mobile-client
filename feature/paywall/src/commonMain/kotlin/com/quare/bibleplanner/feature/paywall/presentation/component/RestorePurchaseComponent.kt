package com.quare.bibleplanner.feature.paywall.presentation.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.already_subscribed
import bibleplanner.feature.paywall.generated.resources.restore_purchase
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RestorePurchaseComponent(
    modifier: Modifier = Modifier,
    onEvent: (PaywallUiEvent) -> Unit,
) {
    val question = stringResource(Res.string.already_subscribed)
    val action = stringResource(Res.string.restore_purchase)

    val annotatedString = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal,
            ),
        ) {
            append("$question ")
        }
        pushStringAnnotation(tag = "restore", annotation = "restore")
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
            ),
        ) {
            append(action)
        }
        pop()
    }

    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotatedString,
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    layoutResult.value?.let { result ->
                        val position = result.getOffsetForPosition(offset)
                        annotatedString
                            .getStringAnnotations(tag = "restore", start = position, end = position)
                            .firstOrNull()
                            ?.let {
                                onEvent(PaywallUiEvent.OnRestorePurchases)
                            }
                    }
                }
            },
        style = MaterialTheme.typography.bodySmall.copy(
            textAlign = TextAlign.Center,
        ),
        onTextLayout = { layoutResult.value = it },
    )
}
