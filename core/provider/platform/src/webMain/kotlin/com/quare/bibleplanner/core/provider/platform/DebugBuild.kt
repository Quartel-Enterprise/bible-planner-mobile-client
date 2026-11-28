package com.quare.bibleplanner.core.provider.platform

private val localHostnames = setOf("localhost", "127.0.0.1")

fun isDebugBuild(): Boolean = getLocationHostname() in localHostnames

@OptIn(ExperimentalWasmJsInterop::class)
private fun getLocationHostname(): String = js("window.location.hostname")
