package com.quare.bibleplanner.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mohamedrejeb.calf.ui.toggle.CupertinoSwitch
import com.mohamedrejeb.calf.ui.toggle.LiquidGlassSwitch
import com.mohamedrejeb.calf.ui.toggle.LiquidGlassSwitchDefaults
import platform.UIKit.UIDevice

private const val LIQUID_GLASS_MAJOR_VERSION = 26
private const val UNCHECKED_TRACK_ARGB = 0x33787878
private val isLiquidGlassAvailable: Boolean =
    (
        UIDevice.currentDevice.systemVersion
            .substringBefore('.')
            .toIntOrNull() ?: 0
    ) >= LIQUID_GLASS_MAJOR_VERSION

@Composable
actual fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    enabled: Boolean,
) {
    val checkedTrackColor = MaterialTheme.colorScheme.primary
    if (isLiquidGlassAvailable) {
        LiquidGlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = LiquidGlassSwitchDefaults.colors(checkedTrackColor = checkedTrackColor),
        )
    } else {
        CupertinoSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = checkedTrackColor,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = Color(UNCHECKED_TRACK_ARGB),
                uncheckedThumbColor = Color.White,
            ),
        )
    }
}
