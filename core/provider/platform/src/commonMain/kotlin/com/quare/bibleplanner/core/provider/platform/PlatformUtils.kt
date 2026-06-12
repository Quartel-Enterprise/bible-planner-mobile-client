package com.quare.bibleplanner.core.provider.platform

fun Platform.isApple(): Boolean = this is Platform.Ios || this is Platform.Desktop.MacOs

fun Platform.isAndroid(): Boolean = this is Platform.Android
