pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
    }
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
