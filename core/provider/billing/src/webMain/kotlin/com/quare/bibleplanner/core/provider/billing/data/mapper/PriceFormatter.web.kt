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

// Why: js() as a whole body is the interop both web targets compile; @JsFun is Wasm only.
@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("UNUSED_PARAMETER")
private fun createCurrencyFormatter(currency: String): CurrencyFormatter =
    js("new Intl.NumberFormat(undefined, { style: 'currency', currency: currency })")
