plugins {
    id("vermilion.android-module-conventions")
}

dependencies {
    implementation(Deps.MATERIAL)
    implementation(Deps.COMPOSE_UI)
    implementation(Deps.COMPOSE_MATERIAL)
    implementation(Deps.COMPOSE_UI_TOOLING_PREVIEW)
    implementation(project(":api"))
    implementation(project(":coreentities"))
    implementation(project(":ui"))
    implementation(project(":utils"))
    implementation(project(":dbentities"))
    implementation(project(":db"))
    debugImplementation(Deps.COMPOSE_UI_TOOLING)
    implementation(Deps.RETROFIT)
    implementation(Deps.RETROFIT_JACKSON_CONVERTER)
    implementation(Deps.SQL_DELIGHT_COROUTINES)
}
