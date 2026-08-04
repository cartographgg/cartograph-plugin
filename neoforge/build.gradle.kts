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
    // api, not implementation: NeoForgePlatform exposes common types (WorldInfo,
    // WorldStatsSnapshot) in its signatures, so they must be visible to the
    // version modules that implement it.
    api(project(":common"))
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

tasks.test {
    useJUnitPlatform()
}
