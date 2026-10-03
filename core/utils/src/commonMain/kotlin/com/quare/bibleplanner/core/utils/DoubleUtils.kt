package com.quare.bibleplanner.core.utils

import kotlin.math.abs
import kotlin.math.round

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
