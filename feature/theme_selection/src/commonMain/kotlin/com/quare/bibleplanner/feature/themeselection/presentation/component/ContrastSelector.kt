package com.quare.bibleplanner.feature.themeselection.presentation.component

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import bibleplanner.feature.theme_selection.generated.resources.Res
import bibleplanner.feature.theme_selection.generated.resources.high_contrast
import bibleplanner.feature.theme_selection.generated.resources.medium_contrast
import bibleplanner.feature.theme_selection.generated.resources.standard_contrast
import com.quare.bibleplanner.ui.theme.model.ContrastType
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContrastSelector(
    modifier: Modifier = Modifier,
    selectedContrast: ContrastType,
    onContrastSelected: (ContrastType) -> Unit,
) {
    val options = getContrastOptions()
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, model ->
            SegmentedButton(
                selected = model.type == selectedContrast,
                onClick = {
                    onContrastSelected(model.type)
                },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                ),
            ) {
                Text(
                    text = stringResource(model.name),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class ContrastOption(
    val name: StringResource,
    val type: ContrastType,
)

private fun getContrastOptions(): List<ContrastOption> = listOf(
    ContrastOption(
        name = Res.string.standard_contrast,
        type = ContrastType.Standard,
    ),
    ContrastOption(
        name = Res.string.medium_contrast,
        type = ContrastType.Medium,
    ),
    ContrastOption(
        name = Res.string.high_contrast,
        type = ContrastType.High,
    ),
)
