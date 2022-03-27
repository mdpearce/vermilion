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
    implementation(Deps.ANDROID_YOUTUBE_PLAYER)
    implementation(Deps.RETROFIT)
    implementation(Deps.JACKSON_ANNOTATIONS)
    implementation(project(":utils"))
    implementation(project(":api"))
    implementation(project(":coreentities"))
    api(Deps.COMMONMARK)
    implementation(Deps.COMMONMARK_EXT_AUTOLINK)
    implementation(Deps.COIL_COMPOSE)
    debugImplementation(Deps.COMPOSE_UI_TOOLING)
    implementation(Deps.KOTLINX_SERIALIZATION_JSON)
    implementation(Deps.ACCOMPANIST_PAGER)
    implementation(Deps.ACCOMPANIST_PAGER_INDICATORS)
}
