pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("compose", "1.3.3")
            version("kotlin", "1.7.21")
            version("sqlDelight", "1.5.4")
            version("hilt", "2.44.2")
            version("accompanist", "0.28.0")
            version("androidGradle", "7.3.0")
        }
    }

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://www.jetbrains.com/intellij-repository/releases")
        maven(url = "https://jetbrains.bintray.com/intellij-third-party-dependencies")
    }
}

rootProject.name = "WishApp"
include(":app")
include(":shared")
