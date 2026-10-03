package com.quare.bibleplanner.core.installattribution.di

import com.quare.bibleplanner.core.installattribution.data.datasource.InstallAttributionLocalDataSource
import com.quare.bibleplanner.core.installattribution.data.datasource.InstallAttributionRemoteDataSource
import com.quare.bibleplanner.core.installattribution.data.mapper.InstallReferrerParser
import com.quare.bibleplanner.core.installattribution.data.repository.InstallAttributionRepositoryImpl
import com.quare.bibleplanner.core.installattribution.domain.repository.InstallAttributionRepository
import com.quare.bibleplanner.core.installattribution.domain.usecase.ReportAppInstall
import com.quare.bibleplanner.core.installattribution.domain.usecase.impl.ReportAppInstallUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val installAttributionModule = module {
    includes(platformInstallAttributionModule)

    factoryOf(::InstallReferrerParser)
    factoryOf(::InstallAttributionLocalDataSource)
    factoryOf(::InstallAttributionRemoteDataSource)
    singleOf(::InstallAttributionRepositoryImpl).bind<InstallAttributionRepository>()

    factoryOf(::ReportAppInstallUseCase).bind<ReportAppInstall>()
}

internal expect val platformInstallAttributionModule: Module
