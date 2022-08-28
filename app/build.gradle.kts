import org.jetbrains.kotlin.konan.properties.loadProperties
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

apply(plugin = "net.thauvin.erik.gradle.semver")
apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")
apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

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



abstract class BuildGoogleServicesJsonTask @Inject constructor() : DefaultTask() {
    @TaskAction
    fun buildGoogleServicesJson() {
        val replaced = project.file("google-services.json.template").readText()
            .replaceTemplate("FB_PROJECT_NUMBER")
            .replaceTemplate("FB_PROJECT_ID")
            .replaceTemplate("FB_STORAGE_BUCKET")
            .replaceTemplate("FB_MOBILESDK_APP_ID")
            .replaceTemplate("FB_OAUTH_CLIENT_ID")
            .replaceTemplate("FB_CURRENT_API_KEY")
            .replaceTemplate("FB_APPINVITE_OAUTH_CLIENT_ID")

        project.file("google-services.json").writeText(replaced)
    }

    private fun getLocalProperties(): Properties {
        val props = Properties()
        if (project.file("local.properties").exists()) {
            props.load(FileInputStream(project.file("local.properties")))
        }
        return props
    }

    private fun getEnvVarOrLocalProperty(propertyName: String): String {
        val envVar = System.getenv(propertyName)
        return if (envVar == null || envVar.isEmpty()) {
            getLocalProperties().getProperty(propertyName)
        } else {
            envVar
        }
    }

    private fun String.replaceTemplate(key: String) =
        replace("<$key>", getEnvVarOrLocalProperty(key))
}

// tasks.register<BuildGoogleServicesJsonTask>("buildGoogleServicesJson")
tasks.register("buildGoogleServicesJson", BuildGoogleServicesJsonTask::class)

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.neaniesoft.vermilion"
        minSdk = 26
        targetSdk = 33
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
        kotlinCompilerExtensionVersion = "1.3.0"
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
    implementation(platform(Deps.FIREBASE_BOM))
    implementation(Deps.FIREBASE_ANALYTICS)
    implementation(Deps.FIREBASE_CRASHLYTICS)
    implementation(Deps.ACCOMPANIST_PAGER)
    implementation(project(":posts"))
    implementation(project(":postdetails"))
    implementation(project(":ui"))
    implementation(project(":uistate"))
    implementation(project(":accounts"))
    implementation(project(":utils"))
    implementation(project(":tabs"))
    implementation(project(":communities"))
    implementation(project(":coreentities"))
    implementation(project(":dbentities"))
    testImplementation(Deps.JUNIT)
    androidTestImplementation(Deps.ANDROIDX_TEST_EXT)
    androidTestImplementation(Deps.ANDROIDX_TEST_ESPRESSO)
    androidTestImplementation(Deps.COMPOSE_UI_TEST_JUNIT4)
    debugImplementation(Deps.COMPOSE_UI_TOOLING_PREVIEW)
    implementation(Deps.KOTLINX_SERIALIZATION_JSON)
}
