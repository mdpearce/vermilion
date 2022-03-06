plugins {
    id("vermilion.android-module-conventions")
}

dependencies {
    implementation(Deps.MATERIAL)
    implementation(Deps.COMPOSE_UI)
    implementation(Deps.COMPOSE_MATERIAL)
    implementation(Deps.COMPOSE_UI_TOOLING_PREVIEW)
    implementation(Deps.LIFECYCLE_RUNTIME)
    implementation(Deps.EXOPLAYER)
    implementation(project(":utils"))
    api(Deps.COMMONMARK)
    implementation(Deps.COMMONMARK_EXT_AUTOLINK)
    implementation(Deps.COIL_COMPOSE)
    debugImplementation(Deps.COMPOSE_UI_TOOLING)
}
