pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public")
    }
}

rootProject.name = "sharp"

include("api")
