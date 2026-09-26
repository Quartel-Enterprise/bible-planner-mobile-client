package com.quare.bibleplanner.core.utils

import kotlin.math.abs
import kotlin.math.round

/**
 * Formats a [Double] as a money string.
 * - If the number has a decimal part, shows 2 decimal places (e.g., 1.50 -> "1.50", 10.99 -> "10.99")
 * - If the number is effectively an integer, shows just the integer value (e.g., 1.0 -> "1", 10.0 -> "10")
 */
fun Double.toMoneyFormat(): String {
    val cents = round(this * 100.0).toLong()
    val sign = if (cents < 0) "-" else ""
    val absoluteCents = abs(cents)
    val integerPart = absoluteCents / 100
    val decimalPart = absoluteCents % 100
    return if (decimalPart == 0L) {
        "$sign$integerPart"
    } else {
        "$sign$integerPart.${decimalPart.toString().padStart(2, '0')}"
    }
}
