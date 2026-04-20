plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    mavenCentral()
}

dependencies {
    implementation(project(":bukkit:spigot"))
    compileOnly(libs.spigot.v261.api)
}

tasks.shadowJar {
    archiveBaseName.set("cartograph-spigot-26.1")
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
