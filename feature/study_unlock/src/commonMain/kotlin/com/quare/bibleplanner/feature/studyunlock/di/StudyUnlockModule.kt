package com.quare.bibleplanner.feature.studyunlock.di

import com.quare.bibleplanner.feature.studyunlock.presentation.viewmodel.StudyUnlockViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureStudyUnlockModule = module {
    viewModelOf(::StudyUnlockViewModel)
}
