plugins {
    id("vermilion.android-module-conventions")
}

dependencies {
    implementation(Deps.MATERIAL)
    implementation(Deps.COMPOSE_UI)
    implementation(Deps.COMPOSE_MATERIAL)
    implementation(Deps.COMPOSE_UI_TOOLING_PREVIEW)
    implementation(Deps.LIFECYCLE_RUNTIME)
    implementation(Deps.RETROFIT)
    implementation(Deps.JACKSON_ANNOTATIONS)
    implementation(Deps.COIL)
    implementation(Deps.COIL_COMPOSE)
    implementation(Deps.COMMONS_TEXT)
    implementation(Deps.PAGING_RUNTIME)
    implementation(Deps.PAGING_COMPOSE)
    implementation(Deps.PRETTY_TIME)
    implementation(Deps.COMMONMARK)
    implementation(project(":ui"))
    implementation(project(":api"))
    implementation(project(":utils"))
    implementation(project(":auth"))
    implementation(project(":dbentities"))
    implementation(project(":db"))
    implementation(project(":posts"))
    debugImplementation(Deps.COMPOSE_UI_TOOLING)

    implementation(Deps.ROOM_RUNTIME)
    kapt(Deps.ROOM_COMPILER)
    implementation(Deps.ROOM_KOTLIN)
    testImplementation(Deps.ROOM_TESTING)
}
