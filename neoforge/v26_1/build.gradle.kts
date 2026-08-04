plugins {
    `java-library`
    alias(libs.plugins.neoforge.moddev)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

neoForge {
    version = libs.versions.neoforge.v261.get()

    runs {
        register("server") {
            server()
            programArgument("--nogui")
        }
    }

    mods {
        register("cartograph") {
            sourceSet(sourceSets["main"])
            sourceSet(project(":neoforge").sourceSets["main"])
            sourceSet(project(":common").sourceSets["main"])
        }
    }
}

dependencies {
    implementation(project(":neoforge"))
    // Jackson via jarJar — NeoForge dedups these nested jars against existing
    // library modules. A plain shade duplicates the jackson.annotation module
    // export and breaks 1.21 (ResolutionException).
    jarJar("com.fasterxml.jackson.core:jackson-databind:[2.18.2,3.0.0)") { version { prefer("2.18.2") } }
    jarJar("com.fasterxml.jackson.core:jackson-core:[2.18.2,3.0.0)") { version { prefer("2.18.2") } }
    jarJar("com.fasterxml.jackson.core:jackson-annotations:[2.18.0,3.0.0)") { version { prefer("2.18.2") } }
}

tasks.named<Jar>("jar") {
    from(project(":neoforge").sourceSets["main"].output)
    from(project(":common").sourceSets["main"].output)
    archiveBaseName.set("cartograph-neoforge-26.1")
}
