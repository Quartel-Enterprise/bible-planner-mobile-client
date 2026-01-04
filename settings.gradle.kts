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
include(":composeApp")
include(":ui:theme")
include(":ui:component")
include(":ui:utils")
include(":core:model")
include(":core:navigation")
include(":core:books")
include(":core:plan")
include(":core:utils")
include(":core:date")
include(":core:provider:koin")
include(":core:provider:platform")
include(":core:provider:room")
include(":core:provider:data_store")
include(":feature:reading_plan")
include(":feature:day")
include(":feature:theme_selection")
include(":feature:material_you")
include(":feature:delete_progress")
include(":feature:delete_notes")
include(":feature:add_notes_free_warning")
include(":feature:edit_plan_start_date")

include(":feature:paywall")
include(":feature:congrats")
include(":core:provider:billing")
include(":core:remote_config")
include(":feature:more")
include(":feature:main")
