package com.quare.bibleplanner.core.provider.platform.domain.usecase

fun interface IsDebugBuild {
    operator fun invoke(): Boolean
}
