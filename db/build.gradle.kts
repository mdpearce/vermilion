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
    implementation(Deps.ROOM_RUNTIME)
    kapt(Deps.ROOM_COMPILER)
    implementation(Deps.ROOM_KOTLIN)
    testImplementation(Deps.ROOM_TESTING)
    implementation(Deps.PAGING_RUNTIME)
    implementation(Deps.ROOM_PAGING)

    implementation(project(":dbentities"))
}
