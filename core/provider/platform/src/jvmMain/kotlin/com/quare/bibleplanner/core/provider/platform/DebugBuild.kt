package com.quare.bibleplanner.core.provider.platform

private const val PACKAGED_APP_PATH_PROPERTY = "jpackage.app-path"

fun isDebugBuild(): Boolean = System.getProperty(PACKAGED_APP_PATH_PROPERTY) == null
