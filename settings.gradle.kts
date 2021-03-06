@file:Suppress("UnstableApiUsage")

include(":app")
include(":posts")
include(":ui")
include(":api")
include(":auth")
include(":accounts")
include(":utils")
include(":postdetails")
include(":db")
include(":dbentities")
include(":tabs")
include(":communities")
include(":coreentities")
include(":uistate")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Vermilion"
