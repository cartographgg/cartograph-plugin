plugins {
    `java-library`
    id("net.neoforged.moddev") version "2.0.141"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

neoForge {
    version = "20.4.251"

    runs {
        register("server") {
            server()
            programArgument("--nogui")
        }
    }

    mods {
        register("cartograph") {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    implementation(project(":common"))
}

tasks.named<Jar>("jar") {
    from(project(":common").sourceSets["main"].output)
    archiveBaseName.set("cartograph-neoforge")
}
