package com.quare.bibleplanner.tools.agentcli.catalog

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

data class RouteDescriptor(
    val name: String,
    val kClass: KClass<out NavKey>,
    val serializer: KSerializer<out NavKey>,
)
