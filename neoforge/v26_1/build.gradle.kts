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
}

tasks.named<Jar>("jar") {
    from(project(":neoforge").sourceSets["main"].output)
    from(project(":common").sourceSets["main"].output)
    archiveBaseName.set("cartograph-neoforge-26.1")
}
