plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 26
        targetSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["appAuthRedirectScheme"] = "com.neaniesoft.vermilion"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.0-rc03"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

kotlin {
    sourceSets.all {
        languageSettings {
            progressiveMode = true // false by default
        }
    }
}

dependencies {
    implementation(Deps.HILT_ANDROID)
    kapt(Deps.HILT_COMPILER)
    androidTestImplementation(Deps.HILT_ANDROID_TESTING)
    kaptAndroidTest(Deps.HILT_COMPILER)
    testImplementation(Deps.HILT_ANDROID_TESTING)
    kaptTest(Deps.HILT_COMPILER)
    implementation(Deps.LIFECYCLE_VIEWMODEL_COMPOSE)
    implementation(Deps.LIFECYCLE_VIEWMODEL)
    implementation(Deps.HILT_NAVIGATION_COMPOSE)
    implementation(Deps.KOTLIN_RESULT)

    implementation(Deps.ANDROIDX_CORE)
    implementation(Deps.ANDROIDX_APPCOMPAT)
    implementation(Deps.KOTLINX_COROUTINES_ANDROID)
    testImplementation(Deps.JUNIT)
    androidTestImplementation(Deps.ANDROIDX_TEST_EXT)
    androidTestImplementation(Deps.ANDROIDX_TEST_ESPRESSO)
}
