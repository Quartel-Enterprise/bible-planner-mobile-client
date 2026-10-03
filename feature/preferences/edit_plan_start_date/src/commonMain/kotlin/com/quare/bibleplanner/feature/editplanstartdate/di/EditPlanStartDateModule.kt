package com.quare.bibleplanner.feature.editplanstartdate.di

import com.quare.bibleplanner.core.plan.domain.usecase.SetPlanStartTimeUseCase
import com.quare.bibleplanner.feature.editplanstartdate.presentation.viewmodel.EditPlanStartDateViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val editPlanStartDateModule = module {
    factoryOf(::SetPlanStartTimeUseCase)

    viewModelOf(::EditPlanStartDateViewModel)
}
