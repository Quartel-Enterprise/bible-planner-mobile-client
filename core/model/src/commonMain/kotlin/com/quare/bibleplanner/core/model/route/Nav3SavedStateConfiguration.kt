package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val nav3SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(MainNavRoute::class, MainNavRoute.serializer())
            subclass(BottomNavRoute.Plans::class, BottomNavRoute.Plans.serializer())
            subclass(BottomNavRoute.Books::class, BottomNavRoute.Books.serializer())
            subclass(BottomNavRoute.More::class, BottomNavRoute.More.serializer())
            subclass(DayNavRoute::class, DayNavRoute.serializer())
            subclass(LogoutNavRoute::class, LogoutNavRoute.serializer())
        }
    }
}
