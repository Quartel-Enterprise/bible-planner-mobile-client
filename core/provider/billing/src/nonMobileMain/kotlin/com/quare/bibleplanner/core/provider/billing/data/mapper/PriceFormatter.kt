package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.provider.billing.data.dto.PriceDto

internal expect class PriceFormatter() {
    fun format(price: PriceDto): String
}
