plugins {
    id("vermilion.android-module-conventions")
}

dependencies {
    implementation(Deps.COMPOSE_UI)
    implementation(Deps.ANDROIDX_SECURITY_CRYPTO)
    implementation(Deps.RETROFIT)
    implementation(Deps.RETROFIT_JACKSON_CONVERTER)
    api(Deps.APP_AUTH)
}
