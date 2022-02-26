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
include(":communitylist")
include(":coreentities")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Vermilion"
