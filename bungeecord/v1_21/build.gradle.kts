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
    maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
    maven("https://libraries.minecraft.net/")
    mavenCentral()
}

dependencies {
    implementation(project(":bungeecord"))
    compileOnly(libs.bungeecord.v121.api)
}

tasks.shadowJar {
    archiveBaseName.set("cartograph-bungeecord-1.21")
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
