@file:OptIn(ExperimentalForeignApi::class)

package com.quare.bibleplanner.ui.component.date

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle

private val wheelsTimePickerSize: DpSize by lazy {
    UIDatePicker()
        .apply {
            datePickerMode = UIDatePickerMode.UIDatePickerModeTime
            preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleWheels
            sizeToFit()
        }.frame
        .useContents { DpSize(size.width.dp, size.height.dp) }
}

internal actual fun Modifier.nativeTimePickerSize(): Modifier = size(wheelsTimePickerSize)
