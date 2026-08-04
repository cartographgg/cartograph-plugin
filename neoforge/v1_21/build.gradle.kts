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
    // NeoForge 21.x already provides jackson-annotation (and possibly more) as
    // module-path libraries; bundling Jackson here duplicates those modules and
    // fails resolution. Testing whether 21.x provides the full stack (incl.
    // databind). 26.1 provides none, so v26_1 jarJars all three.
}

tasks.named<Jar>("jar") {
    from(project(":neoforge").sourceSets["main"].output)
    from(project(":common").sourceSets["main"].output)
    archiveBaseName.set("cartograph-neoforge-1.21")
}
