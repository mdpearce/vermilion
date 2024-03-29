plugins {
    id("vermilion.android-module-conventions")
}

dependencies {
    implementation(Deps.MATERIAL)
    implementation(Deps.COMPOSE_UI)
    implementation(Deps.COMPOSE_MATERIAL)
    implementation(Deps.COMPOSE_UI_TOOLING_PREVIEW)
    implementation(project(":ui"))
    implementation(project(":posts"))
    implementation(project(":postdetails"))
    implementation(project(":utils"))
    implementation(project(":dbentities"))
    implementation(project(":db"))
    implementation(project(":coreentities"))
    implementation(project(":uistate"))
    debugImplementation(Deps.COMPOSE_UI_TOOLING)
    implementation(Deps.SQL_DELIGHT_COROUTINES)
}
