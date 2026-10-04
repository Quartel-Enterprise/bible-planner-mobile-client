package com.quare.bibleplanner.tools.agentcli.json

import org.jetbrains.compose.resources.StringResource

fun interface StringResourceResolver {
    fun resolve(resource: StringResource): String?
}
