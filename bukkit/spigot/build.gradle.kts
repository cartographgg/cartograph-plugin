plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    mavenCentral()
}

dependencies {
    implementation(project(":bukkit"))
    compileOnly(libs.spigot.api)
}

tasks.shadowJar {
    archiveBaseName.set("cartograph-spigot")
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
