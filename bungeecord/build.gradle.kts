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
    maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
    maven("https://libraries.minecraft.net/")
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    compileOnly(libs.bungeecord.api)
}

tasks.shadowJar {
    archiveBaseName.set("cartograph-bungeecord")
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
