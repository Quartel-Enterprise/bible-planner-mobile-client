package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val navigationSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(MainNavRoute::class, MainNavRoute.serializer())
            subclass(MainNavRouteDestination.Plans::class, MainNavRouteDestination.Plans.serializer())
            subclass(MainNavRouteDestination.Books::class, MainNavRouteDestination.Books.serializer())
            subclass(MainNavRouteDestination.More::class, MainNavRouteDestination.More.serializer())
            subclass(AddNotesFreeWarningNavRoute::class, AddNotesFreeWarningNavRoute.serializer())
            subclass(AppLanguageNavRoute::class, AppLanguageNavRoute.serializer())
            subclass(BibleVersionSelectorRoute::class, BibleVersionSelectorRoute.serializer())
            subclass(BookDetailsNavRoute::class, BookDetailsNavRoute.serializer())
            subclass(CongratsNavRoute::class, CongratsNavRoute.serializer())
            subclass(ContactSupportNavRoute::class, ContactSupportNavRoute.serializer())
            subclass(DayNavRoute::class, DayNavRoute.serializer())
            subclass(DeleteAllProgressNavRoute::class, DeleteAllProgressNavRoute.serializer())
            subclass(DeleteNotesRoute::class, DeleteNotesRoute.serializer())
            subclass(DeleteVersionNavRoute::class, DeleteVersionNavRoute.serializer())
            subclass(DonationNavRoute::class, DonationNavRoute.serializer())
            subclass(EditPlanStartDateNavRoute::class, EditPlanStartDateNavRoute.serializer())
            subclass(LoginNavRoute::class, LoginNavRoute.serializer())
            subclass(LoginSyncNudgeNavRoute::class, LoginSyncNudgeNavRoute.serializer())
            subclass(LoginWarningNavRoute::class, LoginWarningNavRoute.serializer())
            subclass(LogoutNavRoute::class, LogoutNavRoute.serializer())
            subclass(MaterialYouBottomSheetNavRoute::class, MaterialYouBottomSheetNavRoute.serializer())
            subclass(NotificationPermissionNavRoute::class, NotificationPermissionNavRoute.serializer())
            subclass(PaywallNavRoute::class, PaywallNavRoute.serializer())
            subclass(PixQrNavRoute::class, PixQrNavRoute.serializer())
            subclass(ReadNavRoute::class, ReadNavRoute.serializer())
            subclass(ReleaseNotesNavRoute::class, ReleaseNotesNavRoute.serializer())
            subclass(SubscriptionDetailsNavRoute::class, SubscriptionDetailsNavRoute.serializer())
            subclass(ThemeNavRoute::class, ThemeNavRoute.serializer())
        }
    }
}
