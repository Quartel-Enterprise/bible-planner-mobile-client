package com.quare.bibleplanner.ui.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.selected
import com.mohamedrejeb.calf.sf.symbols.SFSymbol
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDown
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import com.quare.bibleplanner.ui.icons.AppIcon
import com.quare.bibleplanner.ui.icons.Icon
import org.jetbrains.compose.resources.stringResource

private val anchorSpacing = 8.dp

@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun BoxScope.AppDropdownMenu(
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<AppDropdownMenuItem>,
) {
    AdaptiveDropDown(
        expanded = isExpanded,
        onDismissRequest = onDismissRequest,
        iosItems = items.map { item -> item.toAdaptiveDropDownItem() },
        offset = DpOffset(
            x = 0.dp,
            y = anchorSpacing,
        ),
    ) {
        items.forEach { item ->
            MaterialDropdownMenuItem(item)
        }
    }
}

@Composable
private fun MaterialDropdownMenuItem(item: AppDropdownMenuItem) {
    val contentColor = if (item.isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        LocalContentColor.current
    }
    DropdownMenuItem(
        text = {
            Text(
                text = item.title,
                color = contentColor,
            )
        },
        onClick = item.onClick,
        leadingIcon = item.icon?.let { icon ->
            {
                Icon(
                    icon = icon,
                    contentDescription = null,
                    tint = contentColor,
                )
            }
        },
        trailingIcon = if (item.isSelected) {
            {
                Icon(
                    icon = AppIcon.Check,
                    contentDescription = stringResource(Res.string.selected),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            null
        },
    )
}

private fun AppDropdownMenuItem.toAdaptiveDropDownItem(): AdaptiveDropDownItem {
    val symbolName = if (isSelected) SFSymbol.checkmark else icon?.sfSymbol
    return AdaptiveDropDownItem(
        title = title,
        iosIcon = symbolName?.let(UIKitImage::SystemName),
        isDestructive = isDestructive,
        onClick = onClick,
    )
}
