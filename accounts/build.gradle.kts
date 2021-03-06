plugins {
    id("vermilion.android-module-conventions")
}

dependencies {
    implementation(Deps.ROOM_RUNTIME)
    kapt(Deps.ROOM_COMPILER)
    implementation(Deps.ROOM_KOTLIN)
    testImplementation(Deps.ROOM_TESTING)

    implementation(Deps.MATERIAL)
    implementation(Deps.COMPOSE_UI)
    implementation(Deps.COMPOSE_MATERIAL)
    implementation(Deps.COMPOSE_UI_TOOLING_PREVIEW)
    debugImplementation(Deps.COMPOSE_UI_TOOLING)

    implementation(Deps.KOTLINX_COROUTINES_ANDROID)

    implementation(project(":auth"))
    implementation(project(":api"))
    implementation(project(":ui"))
    implementation(project(":utils"))
    implementation(project(":dbentities"))
    implementation(project(":db"))
    implementation(project(":tabs"))
    implementation(Deps.APP_AUTH)
}
