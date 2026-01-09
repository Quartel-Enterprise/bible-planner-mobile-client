package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.content_description_dismiss
import bibleplanner.feature.books.generated.resources.content_description_info
import bibleplanner.feature.books.generated.resources.reading_not_available_yet
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import org.jetbrains.compose.resources.stringResource

private const val LINK_PRESENTATION = "www.web.bibleplanner.app"

@Composable
internal fun BooksInformationBox(
    onDismiss: () -> Unit,
    onEvent: (BooksUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(Res.string.content_description_info),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )

            HorizontalSpacer(16.dp)

            val fullText = stringResource(Res.string.reading_not_available_yet)
            val primaryColor = MaterialTheme.colorScheme.primary
            val annotatedString = remember(fullText, primaryColor) {
                buildAnnotatedString {
                    append(fullText)
                    val startIndex = fullText.indexOf(LINK_PRESENTATION)
                    if (startIndex != -1) {
                        addLink(
                            clickable = LinkAnnotation.Clickable(
                                tag = "URL",
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = primaryColor,
                                        textDecoration = TextDecoration.Underline,
                                        fontStyle = FontStyle.Italic,
                                    ),
                                ),
                                linkInteractionListener = {
                                    onEvent(BooksUiEvent.OnWebAppLinkClick)
                                },
                            ),
                            start = startIndex,
                            end = startIndex + LINK_PRESENTATION.length,
                        )
                    }
                }
            }

            Text(
                text = annotatedString,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )

            HorizontalSpacer(12.dp)

            CommonIconButton(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.content_description_dismiss),
                onClick = onDismiss,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
