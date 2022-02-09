@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Vermilion"

include(":app")
include(":posts")
include(":ui")
include(":api")
include(":auth")
include(":accounts")
include(":utils")
