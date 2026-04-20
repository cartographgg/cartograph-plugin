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
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    implementation(project(":velocity"))
    compileOnly(libs.velocity.v121.api)
}

tasks.shadowJar {
    archiveBaseName.set("cartograph-velocity-1.21")
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
