package com.quare.bibleplanner.core.inappupdate.data

internal fun interface DeviceRegionProvider {
    fun getRegionCode(): String?
}
