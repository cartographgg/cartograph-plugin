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
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    implementation(project(":bukkit"))
    compileOnly(libs.paper.api)
}

tasks.shadowJar {
    archiveBaseName.set("cartograph-paper")
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
