package com.quare.bibleplanner.feature.chapterstudy.di

import com.quare.bibleplanner.feature.chapterstudy.domain.usecase.ChapterStudyUseCases
import com.quare.bibleplanner.feature.chapterstudy.presentation.viewmodel.ChapterStudyViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureChapterStudyModule = module {
    factoryOf(::ChapterStudyUseCases)
    viewModelOf(::ChapterStudyViewModel)
}
