package com.quare.bibleplanner.core.installattribution.data.mapper

import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.parseQueryString

internal class InstallReferrerParser {
    fun parseOppref(referrer: String?): String? {
        if (referrer.isNullOrBlank()) return null
        // Why: a Play link encoded twice hands the referrer back still encoded once (`oppref%3DXYZ%26...`).
        return findOppref(referrer) ?: findOppref(referrer.decodeURLQueryComponent())
    }

    private fun findOppref(query: String): String? = parseQueryString(query)[OPPREF_PARAMETER]
        ?.trim()
        ?.takeIf(String::isNotEmpty)

    private companion object {
        const val OPPREF_PARAMETER = "oppref"
    }
}
