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
    implementation(project(":common"))
    compileOnly(libs.velocity.api)
    compileOnly(libs.snakeyaml)
    annotationProcessor(libs.velocity.api)
}

tasks.shadowJar {
    archiveBaseName.set("cartograph-velocity")
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
