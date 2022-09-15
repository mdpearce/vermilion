plugins {
    id("vermilion.android-module-conventions")
}

apply(plugin = "com.squareup.sqldelight")

android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += "room.schemaLocation" to "$projectDir/schemas"
            }
        }
    }
}

dependencies {
    implementation(Deps.PAGING_RUNTIME)

    implementation(Deps.SQL_DELIGHT_ANDROID_DRIVER)

    implementation(project(":dbentities"))
}
