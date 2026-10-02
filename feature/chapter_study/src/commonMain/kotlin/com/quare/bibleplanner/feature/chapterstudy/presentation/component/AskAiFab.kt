package com.quare.bibleplanner.feature.chapterstudy.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.chapter_study.generated.resources.Res
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_chat
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AskAiFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(Res.string.chapter_study_chat)
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Forum,
                contentDescription = label,
            )
        },
        text = { Text(text = label) },
    )
}
