package com.quare.bibleplanner.feature.read.di

import com.quare.bibleplanner.feature.read.data.repository.ReaderSettingsRepositoryImpl
import com.quare.bibleplanner.feature.read.domain.repository.ReaderSettingsRepository
import com.quare.bibleplanner.feature.read.domain.usecase.GetNextChapter
import com.quare.bibleplanner.feature.read.domain.usecase.GetReadNavigationSuggestionsModelUseCase
import com.quare.bibleplanner.feature.read.domain.usecase.ObserveReaderSettings
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFont
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFontSize
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderVerticalReading
import com.quare.bibleplanner.feature.read.domain.usecase.impl.GetNextChapterUseCase
import com.quare.bibleplanner.feature.read.domain.usecase.impl.ObserveReaderSettingsUseCase
import com.quare.bibleplanner.feature.read.domain.usecase.impl.SetReaderFocusAidUseCase
import com.quare.bibleplanner.feature.read.domain.usecase.impl.SetReaderFontSizeUseCase
import com.quare.bibleplanner.feature.read.domain.usecase.impl.SetReaderFontUseCase
import com.quare.bibleplanner.feature.read.domain.usecase.impl.SetReaderRulerLinesUseCase
import com.quare.bibleplanner.feature.read.domain.usecase.impl.SetReaderVerticalReadingUseCase
import com.quare.bibleplanner.feature.read.presentation.ReadViewModel
import com.quare.bibleplanner.feature.read.presentation.appearance.ReaderAppearanceViewModel
import com.quare.bibleplanner.feature.read.presentation.deletecolor.DeleteHighlightColorViewModel
import com.quare.bibleplanner.feature.read.presentation.factory.ObserveReadData
import com.quare.bibleplanner.feature.read.presentation.factory.ReadDataPresentationModelFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val featureReadModule = module {
    singleOf(::ReaderSettingsRepositoryImpl).bind<ReaderSettingsRepository>()

    factoryOf(::ObserveReaderSettingsUseCase).bind<ObserveReaderSettings>()
    factoryOf(::SetReaderFontSizeUseCase).bind<SetReaderFontSize>()
    factoryOf(::SetReaderFontUseCase).bind<SetReaderFont>()
    factoryOf(::SetReaderFocusAidUseCase).bind<SetReaderFocusAid>()
    factoryOf(::SetReaderRulerLinesUseCase).bind<SetReaderRulerLines>()
    factoryOf(::GetNextChapterUseCase).bind<GetNextChapter>()
    factoryOf(::SetReaderVerticalReadingUseCase).bind<SetReaderVerticalReading>()

    factoryOf(::GetReadNavigationSuggestionsModelUseCase)
    factoryOf(::ReadDataPresentationModelFactory).bind<ObserveReadData>()
    viewModelOf(::ReadViewModel)
    viewModelOf(::ReaderAppearanceViewModel)
    viewModelOf(::DeleteHighlightColorViewModel)
}
