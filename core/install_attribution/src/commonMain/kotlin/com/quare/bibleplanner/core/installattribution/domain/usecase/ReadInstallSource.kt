package com.quare.bibleplanner.core.installattribution.domain.usecase

import com.quare.bibleplanner.core.installattribution.domain.model.InstallSource

fun interface ReadInstallSource {
    suspend operator fun invoke(): InstallSource?
}
