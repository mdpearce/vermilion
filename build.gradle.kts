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
        classpath(Deps.KOTLINTER_GRADLE_PLUGIN)
        classpath(Deps.SEMVER)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id("com.dipien.releaseshub.gradle.plugin").version("3.1.0")
}

subprojects {
    apply(plugin = "org.jmailen.kotlinter")
}

tasks.create("clean", Delete::class) {
    this.setDelete(rootProject.buildDir)
}

fun getLocalProperties(): java.util.Properties {
    val props = java.util.Properties()
    if (file("custom.local.properties").exists()) {
        props.load(java.io.FileInputStream(file("custom.local.properties")))
    }
    return props
}

fun getEnvVarOrLocalProperty(propertyName: String): String {
    val envVar = System.getenv(propertyName)
    return if (envVar == null || envVar.isEmpty()) {
        getLocalProperties().getProperty(propertyName)
    } else {
        envVar
    }
}

releasesHub {
    dependenciesPaths = listOf("buildSrc/src/main/kotlin/Deps.kt")
    excludes = listOf("com.squareup.okhttp3")

    pullRequestEnabled = true
    pullRequestsMax = 15
    gitHubRepositoryOwner = getEnvVarOrLocalProperty("GH_REPO_OWNER")
    gitHubRepositoryName = getEnvVarOrLocalProperty("GH_REPO_NAME")
    gitHubWriteToken = getEnvVarOrLocalProperty("GH_WRITE_TOKEN")
    gitUserName = getEnvVarOrLocalProperty("GIT_USER_NAME")
    gitUserEmail = getEnvVarOrLocalProperty("GIT_USER_EMAIL")
    baseBranch = "main"
}
