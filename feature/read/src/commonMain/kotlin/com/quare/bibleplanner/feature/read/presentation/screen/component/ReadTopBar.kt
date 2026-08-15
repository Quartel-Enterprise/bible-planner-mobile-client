package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.reader_appearance
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.stringResource

/**
 * The title only appears once the chapter's own oversized header has scrolled away, so the two never
 * name the chapter at the same time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadTopBar(
    platform: Platform,
    header: ReadHeaderUiModel,
    isTitleVisible: Boolean,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    onEvent: (ReadUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val bookName = stringResource(header.bookStringResource)
    TopAppBar(
        modifier = modifier,
        scrollBehavior = topAppBarScrollBehavior,
        title = {
            AnimatedVisibility(
                visible = isTitleVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Text(text = "$bookName ${header.chapterNumber}")
            }
        },
        navigationIcon = {
            BackIcon(
                platform = platform,
                onBackClick = { onEvent(ReadUiEvent.OnArrowBackClick) },
            )
        },
        actions = {
            CommonIconButton(
                imageVector = Icons.Default.TextFormat,
                contentDescription = stringResource(Res.string.reader_appearance),
                onClick = { onEvent(ReadUiEvent.OnAppearanceClick) },
            )
            BibleVersionChip(
                versionName = header.versionAbbreviation,
                onClick = { onEvent(ReadUiEvent.ManageBibleVersions) },
            )
        },
    )
}
