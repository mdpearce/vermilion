plugins {
    id("vermilion.android-module-conventions")
}

dependencies {
    implementation(Deps.ROOM_RUNTIME)
    kapt(Deps.ROOM_COMPILER)
    implementation(Deps.ROOM_KOTLIN)
    testImplementation(Deps.ROOM_TESTING)
}