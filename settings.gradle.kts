@file:Suppress("UnstableApiUsage")

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }


    versionCatalogs {
        create("libs") {
            version("kotlin", "1.6.10")
            version("daggerHilt", "2.40.5")
            alias("android-gradle-plugin").to("com.android.tools.build:gradle:7.1.1")
            alias("kotlin-gradle-plugin").to("org.jetbrains.kotlin", "kotlin-gradle-plugin").versionRef("kotlin")
            alias("hilt-android-gradle-plugin").to("com.google.dagger", "hilt-android-gradle-plugin")
                .versionRef("daggerHilt")
        }
    }
}
rootProject.name = "Vermilion"
include(":app")
include(":posts")
include(":ui")
include(":api")
include(":auth")
