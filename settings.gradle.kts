pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "cartograph-plugin"

include(":common")
include(":bukkit")
include(":bukkit:spigot")
include(":bukkit:paper")
include(":bukkit:folia")
include(":velocity")
include(":bungeecord")
include(":forge")
include(":neoforge")
