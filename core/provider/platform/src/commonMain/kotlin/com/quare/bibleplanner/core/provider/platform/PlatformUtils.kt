package com.quare.bibleplanner.core.provider.platform

fun Platform.isApple(): Boolean = this == Platform.IOS || this == Platform.MACOS

fun Platform.isAndroid(): Boolean = this == Platform.ANDROID
