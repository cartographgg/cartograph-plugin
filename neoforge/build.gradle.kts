plugins {
    `java-library`
    alias(libs.plugins.neoforge.moddev)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

neoForge {
    version = libs.versions.neoforge.v121.get()
}

dependencies {
    implementation(project(":common"))
}
