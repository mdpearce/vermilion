import java.util.Properties
import java.io.FileInputStream

plugins {
    id("vermilion.android-module-conventions")
}

dependencies {
    implementation(project(":auth"))
    implementation(project(":utils"))

    implementation(Deps.COMPOSE_UI)
    implementation(Deps.RETROFIT)
    implementation(Deps.RETROFIT_JACKSON_CONVERTER)
    implementation(Deps.JACKSON_MODULE_KOTLIN)
    implementation(Deps.OKHTTP)
    implementation(Deps.OKHTTP_LOGGING_INTERCEPTOR)
}

fun getLocalProperties(): Properties {
    val props = Properties()
    if (file("local.properties").exists()) {
        props.load(FileInputStream(file("local.properties")))
    }
    return props
}

fun getEnvVarOrLocalProperty(propertyName: String): String {
    val envVar = System.getenv(propertyName)
    return if (envVar == null || envVar.isEmpty()) {
        getLocalProperties().getProperty(propertyName) ?: ""
    } else {
        envVar
    }
}

android {
    buildTypes {
        debug {
            resValue("string", "reddit_api_client_id", getEnvVarOrLocalProperty("REDDIT_API_CLIENT_ID"))
        }
        release {
            resValue("string", "reddit_api_client_id", getEnvVarOrLocalProperty("REDDIT_API_CLIENT_ID"))
        }
    }
}
