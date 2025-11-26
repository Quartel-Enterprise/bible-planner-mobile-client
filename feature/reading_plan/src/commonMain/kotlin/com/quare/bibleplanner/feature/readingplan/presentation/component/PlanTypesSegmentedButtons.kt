package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.book_order
import bibleplanner.feature.reading_plan.generated.resources.chronological_order
import com.quare.bibleplanner.feature.readingplan.domain.model.ReadingPlanType
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanPresentationModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlanTypesSegmentedButtons(
    modifier: Modifier = Modifier,
    selectedReadingPlan: ReadingPlanType,
    onPlanClick: (ReadingPlanType) -> Unit,
) {
    val typeList = getTypeList()
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        typeList.forEachIndexed { index, model ->
            SegmentedButton(
                selected = model.type == selectedReadingPlan,
                onClick = {
                    onPlanClick(model.type)
                },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = typeList.size,
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

@Composable
private fun getTypeList(): List<ReadingPlanPresentationModel> = ReadingPlanType.entries.map {
    it.toPresentationModel()
}

@Composable
private fun ReadingPlanType.toPresentationModel(): ReadingPlanPresentationModel = when (this) {
    ReadingPlanType.CHRONOLOGICAL -> ReadingPlanPresentationModel(
        name = Res.string.chronological_order,
        type = this,
    )

    ReadingPlanType.BOOKS -> ReadingPlanPresentationModel(
        name = Res.string.book_order,
        type = this,
    )
}
