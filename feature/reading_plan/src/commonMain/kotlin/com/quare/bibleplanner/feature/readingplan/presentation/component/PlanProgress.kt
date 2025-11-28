package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.loading
import bibleplanner.feature.reading_plan.generated.resources.progress_label
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.round

@Composable
fun PlanProgress(
    isLoading: Boolean,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column {
            Text(
                text = stringResource(
                    Res.string.progress_label,
                    if (isLoading) {
                        stringResource(Res.string.loading)
                    } else {
                        progress.formatProgress()
                    },
                ),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp),
            )
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = { progress / 100 },
            )
        }
    }
}

private fun Float.formatProgress(): String {
    // Round to 2 decimal places
    val rounded = round(this * 100.0f) / 100.0f

    // Check if it has no decimal places (is an integer)
    val tolerance = 0.0001f
    if (abs(rounded % 1.0f) < tolerance) {
        return rounded.toInt().toString()
    }

    // Get the integer and decimal parts
    val integerPart = rounded.toInt()
    val decimalPart = rounded - integerPart

    // Multiply by 100 to get the first two decimal places as integer
    val decimalAsInt = (decimalPart * 100.0f + 0.5f).toInt()

    // If the second decimal place is 0, it only has 1 significant decimal place
    if (decimalAsInt % 10 == 0) {
        val firstDecimal = decimalAsInt / 10
        return "$integerPart.$firstDecimal"
    }

    // If it has 2 or more decimal places, return with 2 places rounded
    val firstDecimal = decimalAsInt / 10
    val secondDecimal = decimalAsInt % 10
    return "$integerPart.$firstDecimal$secondDecimal"
}
