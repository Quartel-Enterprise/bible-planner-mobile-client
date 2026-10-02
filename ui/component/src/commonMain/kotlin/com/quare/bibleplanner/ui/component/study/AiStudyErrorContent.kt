package com.quare.bibleplanner.ui.component.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.ai_study_connection_error_message
import bibleplanner.ui.component.generated.resources.ai_study_error
import bibleplanner.ui.component.generated.resources.ai_study_generation_error_title
import bibleplanner.ui.component.generated.resources.ai_study_retry
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

private val descriptionMaxWidth = 300.dp

@Composable
fun AiStudyErrorContent(
    isOffline: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 38.dp,
                vertical = 40.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AiStudyHeroIcon(icon = Icons.Rounded.CloudOff)
        VerticalSpacer(20)
        Text(
            text = stringResource(Res.string.ai_study_generation_error_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        VerticalSpacer(10)
        Text(
            text = stringResource(
                if (isOffline) Res.string.ai_study_connection_error_message else Res.string.ai_study_error,
            ),
            modifier = Modifier.widthIn(max = descriptionMaxWidth),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(24)
        Button(
            onClick = onRetryClick,
            modifier = Modifier.height(50.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            HorizontalSpacer(ButtonDefaults.IconSpacing)
            Text(text = stringResource(Res.string.ai_study_retry))
        }
    }
}
