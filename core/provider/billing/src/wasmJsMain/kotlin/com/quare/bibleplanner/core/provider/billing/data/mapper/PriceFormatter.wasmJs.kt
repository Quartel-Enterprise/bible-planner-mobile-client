package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.provider.billing.data.dto.PriceDto

internal actual class PriceFormatter {
    actual fun format(price: PriceDto): String {
        val amount = price.amountMicros / MICROS_PER_UNIT
        return runCatching { createCurrencyFormatter(price.currency).format(amount) }
            .getOrDefault("${price.currency} $amount")
    }

    private companion object {
        const val MICROS_PER_UNIT = 1_000_000.0
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private external class CurrencyFormatter : JsAny {
    fun format(amount: Double): String
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(currency) => new Intl.NumberFormat(undefined, { style: 'currency', currency: currency })")
private external fun createCurrencyFormatter(currency: String): CurrencyFormatter
