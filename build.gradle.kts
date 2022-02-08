@file:Suppress("UnstableApiUsage")

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(Deps.ANDROID_GRADLE_PLUGIN)
        classpath(Deps.KOTLIN_GRADLE_PLUGIN)
        classpath(Deps.HILT_ANDROID_GRADLE_PLUGIN)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

tasks.create("clean", Delete::class) {
    this.setDelete(rootProject.buildDir)
}