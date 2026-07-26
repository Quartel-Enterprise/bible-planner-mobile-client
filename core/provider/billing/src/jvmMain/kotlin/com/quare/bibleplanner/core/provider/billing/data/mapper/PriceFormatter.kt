package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.provider.billing.data.dto.PriceDto
import java.text.NumberFormat
import java.util.Currency

internal class PriceFormatter {
    fun format(price: PriceDto): String {
        val amount = price.amountMicros / MICROS_PER_UNIT
        val currency = runCatching { Currency.getInstance(price.currency) }.getOrNull()
            ?: return "${price.currency} $amount"
        return NumberFormat
            .getCurrencyInstance()
            .apply { this.currency = currency }
            .format(amount)
    }

    private companion object {
        const val MICROS_PER_UNIT = 1_000_000.0
    }
}
