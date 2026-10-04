package com.quare.bibleplanner.tools.agentcli.json

import kotlin.reflect.KType

private val packagePrefix = Regex("""\b(?:[a-z_][a-z0-9_]*\.)+(?=[A-Z])""")

fun KType.toDisplayName(): String = toString().replace(packagePrefix, "")
