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
            version("compose", "1.1.0-rc03")
            version("lifecycle", "2.5.0-alpha01")
            alias("android-gradle-plugin").to("com.android.tools.build:gradle:7.1.1")
            alias("kotlin-gradle-plugin").to("org.jetbrains.kotlin", "kotlin-gradle-plugin").versionRef("kotlin")
            alias("hilt-android-gradle-plugin").to("com.google.dagger", "hilt-android-gradle-plugin")
                .versionRef("daggerHilt")
            alias("hilt-android").to("com.google.dagger", "hilt-android").versionRef("daggerHilt")
            alias("hilt-compiler").to("com.google.dagger", "hilt-compiler").versionRef("daggerHilt")
            alias("hilt-android-testing").to("com.google.dagger", "hilt-android-testing").versionRef("daggerHilt")
            alias("androidx-navigation-compose").to("androidx.navigation:navigation-compose:2.5.0-alpha01")
            alias("androidx-core").to("androidx.core:core-ktx:1.7.0")
            alias("androidx-appcompat").to("androidx.appcompat:appcompat:1.4.1")
            alias("android-material").to("com.google.android.material:material:1.5.0")
            alias("androidx-compose-ui").to("androidx.compose.ui", "ui").versionRef("compose")
            alias("androidx-compose-material").to("androidx.compose.material", "material").versionRef("compose")
            alias("androidx-compose-ui-tooling-preview").to("androidx.compose.ui", "ui-tooling-preview").versionRef("compose")
            alias("androidx-lifecycle-runtime").to("androidx.lifecycle", "lifecycle-runtime-ktx").versionRef("lifecycle")
            alias("androidx-activity-compose").to("androidx.activity:activity-compose:1.5.0-alpha01")
            alias("hilt-navigation-compose").to("androidx.hilt:hilt-navigation-compose:1.0.0")
            alias("kotlinx-coroutines-android").to("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
            alias("junit").to("junit:junit:4.13.2")
            alias("androidx-test-ext").to("androidx.test.ext:junit:1.1.3")
            alias("androidx-test-espresso-core").to("androidx.test.espresso:espresso-core:3.4.0")
            alias("androidx-compose-ui-test-junit4").to("androidx.compose.ui", "ui-test-junit4").versionRef("compose")
        }
    }
}
rootProject.name = "Vermilion"
include(":app")
include(":posts")
include(":ui")
include(":api")
include(":auth")
