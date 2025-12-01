package com.quare.bibleplanner.core.provider.platform

fun Platform.isApplePlatform(): Boolean = this == Platform.IOS || this == Platform.MACOS

fun Platform.isAndroid(): Boolean = this == Platform.ANDROID
