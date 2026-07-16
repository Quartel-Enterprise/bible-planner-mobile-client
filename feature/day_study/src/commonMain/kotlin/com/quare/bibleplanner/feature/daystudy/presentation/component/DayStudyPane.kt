package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_generate
import bibleplanner.feature.day_study.generated.resources.ai_study_subscribe
import bibleplanner.feature.day_study.generated.resources.ai_study_title
import bibleplanner.feature.day_study.generated.resources.ai_study_view
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

private val descriptionMaxWidth = 300.dp

@Composable
internal fun DayStudyPane(
    cardState: Loadable<DayStudyCardUiModel>,
    openStudy: DayStudyModel?,
    generation: DayStudyGenerationUiModel?,
    isOpeningStudy: Boolean,
    onCardClick: () -> Unit,
    showStudyHeader: Boolean,
    modifier: Modifier = Modifier,
) {
    val mode = cardState.valueOrNull()?.mode
    LaunchedEffect(mode, openStudy != null, generation != null) {
        if (openStudy == null && generation == null && mode == DayStudyCardMode.VIEW) {
            onCardClick()
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        when {
            generation != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center,
            ) {
                DayStudyGeneratingContent(
                    generation = generation,
                    modifier = Modifier.padding(34.dp),
                )
            }

            openStudy != null -> DayStudyInlinePane(
                study = openStudy,
                showHeader = showStudyHeader,
                modifier = Modifier.fillMaxSize(),
            )

            cardState is Loadable.Loaded -> DayStudyPaneHero(
                card = cardState.value,
                isOpening = isOpeningStudy,
                onClick = onCardClick,
                modifier = Modifier.fillMaxSize(),
            )

            else -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun DayStudyPaneHero(
    card: DayStudyCardUiModel,
    isOpening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLocked = card.mode == DayStudyCardMode.LOCKED
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
        HeroIcon(icon = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.AutoAwesome)
        VerticalSpacer(20)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.ai_study_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            CardBadge(card)
        }
        VerticalSpacer(10)
        Text(
            text = dayStudyCardSubtitle(card),
            modifier = Modifier.widthIn(max = descriptionMaxWidth),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(24)
        HeroButton(
            mode = card.mode,
            isLoading = isOpening,
            onClick = onClick,
        )
    }
}

@Composable
private fun HeroIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun HeroButton(
    mode: DayStudyCardMode,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.height(50.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = heroButtonIcon(mode),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            HorizontalSpacer(ButtonDefaults.IconSpacing)
            Text(text = stringResource(heroButtonLabel(mode)))
        }
    }
}

private fun heroButtonIcon(mode: DayStudyCardMode): ImageVector = when (mode) {
    DayStudyCardMode.LOCKED -> Icons.Rounded.LockOpen
    DayStudyCardMode.GENERATE, DayStudyCardMode.VIEW -> Icons.Rounded.AutoAwesome
}

private fun heroButtonLabel(mode: DayStudyCardMode) = when (mode) {
    DayStudyCardMode.GENERATE -> Res.string.ai_study_generate
    DayStudyCardMode.VIEW -> Res.string.ai_study_view
    DayStudyCardMode.LOCKED -> Res.string.ai_study_subscribe
}
