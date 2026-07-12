rootProject.name = "BiblePlanner"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":androidApp")
include(":shared")
include(":desktopApp")
include(":ui:theme")
include(":ui:component")
include(":ui:utils")
include(":core:model")
include(":core:navigation")
include(":core:books")
include(":core:plan")
include(":core:clear")
include(":core:sync")
include(":core:utils")
include(":core:user")
include(":core:date")
include(":core:devices")
include(":core:provider:koin")
include(":core:provider:platform")
include(":core:provider:connectivity")
include(":core:provider:room")
include(":core:provider:data_store")
include(":core:provider:supabase")
include(":core:provider:analytics")
include(":core:provider:crashlytics")
include(":feature:reading_plan")
include(":feature:day")
include(":feature:day_study")
include(":feature:preferences:theme_selection")
include(":feature:material_you")
include(":feature:delete_progress")
include(":feature:delete_version")
include(":feature:delete_notes")
include(":feature:logout")
include(":feature:add_notes_free_warning")
include(":feature:preferences:edit_plan_start_date")
include(":feature:login")
include(":feature:login_warning")
include(":feature:login_sync_nudge")
include(":core:login_nudge")
include(":feature:preferences:bible_version")
include(":feature:preferences:app_language")
include(":feature:read")

include(":feature:paywall")
include(":feature:congrats")
include(":feature:subscription_details")
include(":feature:account_details")
include(":core:provider:billing")
include(":core:provider:language")
include(":core:remote_config")
include(":feature:contact_support")
include(":feature:more")
include(":feature:main")
include(":feature:books")
include(":feature:book_details")
include(":feature:release_notes")
include(":feature:donation")
include(":feature:donation:pix_qr")
include(":core:utils:json_reader")
include(":core:network")
include(":core:notification")
include(":feature:notification_permission")
include(":feature:in_app_update")
include(":tools:ktlint-custom-rules")
