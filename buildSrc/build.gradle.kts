plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    //TODO: Replace these hardcoded values with centralized dependencies
    implementation("com.android.library:com.android.library.gradle.plugin:7.1.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.40.5")
}