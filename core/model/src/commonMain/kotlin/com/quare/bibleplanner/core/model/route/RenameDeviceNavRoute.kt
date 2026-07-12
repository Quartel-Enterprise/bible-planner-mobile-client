package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class RenameDeviceNavRoute(
    val deviceRowId: String,
    val currentName: String,
) : NavRoute
