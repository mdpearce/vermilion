import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

apply(plugin = "net.thauvin.erik.gradle.semver")

// TODO This is pretty gross
fun versionNameFromSemVer(): String {
    val versionProps = loadProperties("app/version.properties")
    return versionProps["version.semver"] as String
}

fun versionCodeFromSemVer(): Int {
    val versionProps = loadProperties("app/version.properties")
    val major = versionProps["version.major"] as String
    val minor = versionProps["version.minor"] as String
    val patch = versionProps["version.patch"] as String

    return (major + minor.padStart(3, '0') + patch.padStart(3, '0')).toInt()
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.neaniesoft.vermilion"
        minSdk = 26
        targetSdk = 31
        versionCode = versionCodeFromSemVer()
        versionName = versionNameFromSemVer()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        manifestPlaceholders["appAuthRedirectScheme"] = "com.neaniesoft.vermilion"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.0-rc03"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Dagger Hilt (for dependency injection)
    implementation(Deps.HILT_ANDROID)
    kapt(Deps.HILT_COMPILER)
    androidTestImplementation(Deps.HILT_ANDROID_TESTING)
    kaptAndroidTest(Deps.HILT_COMPILER)
    testImplementation(Deps.HILT_ANDROID_TESTING)
    kaptTest(Deps.HILT_COMPILER)

    implementation(Deps.ANDROIDX_NAVIGATION_COMPOSE)
    implementation(Deps.ANDROIDX_CORE)
    implementation(Deps.ANDROIDX_APPCOMPAT)
    implementation(Deps.MATERIAL)
    implementation(Deps.COMPOSE_UI)
    implementation(Deps.COMPOSE_MATERIAL)
    implementation(Deps.COMPOSE_UI_TOOLING_PREVIEW)
    implementation(Deps.LIFECYCLE_RUNTIME)
    implementation(Deps.ANDROIDX_ACTIVITY_COMPOSE)
    implementation(Deps.HILT_NAVIGATION_COMPOSE)
    implementation(Deps.KOTLINX_COROUTINES_ANDROID)
    implementation(Deps.ACCOMPANIST_NAVIGATION_MATERIAL)
    implementation(Deps.KOTLIN_RESULT)
    implementation(Deps.PAGING_RUNTIME)
    implementation(Deps.PAGING_COMPOSE)
    implementation(Deps.ANDROIDX_BROWSER)
    implementation(project(":posts"))
    implementation(project(":postdetails"))
    implementation(project(":ui"))
    implementation(project(":accounts"))
    implementation(project(":utils"))
    implementation(project(":tabs"))
    testImplementation(Deps.JUNIT)
    androidTestImplementation(Deps.ANDROIDX_TEST_EXT)
    androidTestImplementation(Deps.ANDROIDX_TEST_ESPRESSO)
    androidTestImplementation(Deps.COMPOSE_UI_TEST_JUNIT4)
    debugImplementation(Deps.COMPOSE_UI_TOOLING_PREVIEW)
}
