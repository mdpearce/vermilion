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
    implementation(project(":ui"))
    implementation(project(":api"))
    implementation(project(":utils"))
    debugImplementation(Deps.COMPOSE_UI_TOOLING)
}