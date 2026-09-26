package com.quare.bibleplanner.core.date

fun interface CurrentTimestampProvider {
    fun getCurrentTimestamp(): Long
}
