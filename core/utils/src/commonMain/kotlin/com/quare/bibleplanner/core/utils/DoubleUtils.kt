package com.quare.bibleplanner.core.utils

import kotlin.math.abs

/**
 * Formats a [Double] as a money string.
 * - If the number has a decimal part, shows 2 decimal places (e.g., 1.50 -> "1.50", 10.99 -> "10.99")
 * - If the number is effectively an integer, shows just the integer value (e.g., 1.0 -> "1", 10.0 -> "10")
 */
fun Double.toMoneyFormat(): String {
    // Round to 2 decimal places
    val rounded = kotlin.math.round(this * 100.0) / 100.0
    val tolerance = 0.0001

    // Check if the number is effectively an integer
    if (abs(rounded % 1.0) < tolerance) {
        return rounded.toInt().toString()
    }

    // Format with 2 decimal places manually (KMP-compatible)
    val integerPart = rounded.toInt()
    val decimalPart = abs((rounded - integerPart) * 100.0).toInt()
    val decimalString = if (decimalPart < 10) {
        "0$decimalPart"
    } else {
        decimalPart.toString()
    }
    return "$integerPart.$decimalString"
}
