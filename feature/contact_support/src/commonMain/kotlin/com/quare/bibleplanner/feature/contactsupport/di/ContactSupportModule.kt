package com.quare.bibleplanner.feature.contactsupport.di

import com.quare.bibleplanner.feature.contactsupport.presentation.factory.BuildSupportEmailBody
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.ContactSupportMailtoFactory
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.ContactSupportUiStateFactory
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.EncodeMailtoComponent
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.impl.BuildSupportEmailBodyImpl
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.impl.ContactSupportMailtoFactoryImpl
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.impl.EncodeMailtoComponentImpl
import com.quare.bibleplanner.feature.contactsupport.presentation.viewmodel.ContactSupportViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val contactSupportModule = module {
    factoryOf(::ContactSupportUiStateFactory)
    factoryOf(::BuildSupportEmailBodyImpl).bind<BuildSupportEmailBody>()
    factoryOf(::EncodeMailtoComponentImpl).bind<EncodeMailtoComponent>()
    factoryOf(::ContactSupportMailtoFactoryImpl).bind<ContactSupportMailtoFactory>()
    viewModelOf(::ContactSupportViewModel)
}
