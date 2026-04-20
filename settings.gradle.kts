pluginManagement {
    repositories {
        gradlePluginPortal()
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
include(":bukkit:spigot:v1_21")
include(":bukkit:spigot:v26_1")
include(":bukkit:paper")
include(":bukkit:paper:v1_21")
include(":bukkit:paper:v26_1")
include(":bukkit:folia")
include(":bukkit:folia:v1_21")
include(":bukkit:folia:v26_1")
include(":bungeecord")
include(":bungeecord:v1_21")
include(":bungeecord:v26_1")
include(":velocity")
include(":velocity:v1_21")
include(":velocity:v26_1")
include(":neoforge")
include(":neoforge:v1_21")
include(":neoforge:v26_1")
