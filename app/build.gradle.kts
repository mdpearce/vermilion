plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.neaniesoft.vermilion"
        minSdk = 26
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(project(":posts"))
    implementation(project(":ui"))
    implementation(project(":accounts"))
    testImplementation(Deps.JUNIT)
    androidTestImplementation(Deps.ANDROIDX_TEST_EXT)
    androidTestImplementation(Deps.ANDROIDX_TEST_ESPRESSO)
    androidTestImplementation(Deps.COMPOSE_UI_TEST_JUNIT4)
    debugImplementation(Deps.COMPOSE_UI_TOOLING_PREVIEW)
}