package com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LocalDate.toShortLabel(): String {
    val dayLabel = day.toString().padStart(2, '0')
    val monthLabel = stringResource(month.toStringResource()).take(3)
    return "$dayLabel $monthLabel"
}
