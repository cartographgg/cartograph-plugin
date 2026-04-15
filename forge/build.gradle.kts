plugins {
    java
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    alias(libs.plugins.shadow)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

configure<net.minecraftforge.gradle.userdev.UserDevExtension> {
    mappings("official", "1.20.1")

    runs {
        create("server") {
            workingDirectory(project.file("run"))
            args("--nogui")
            mods {
                create("cartograph") {
                    source(sourceSets.main.get())
                }
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("net.minecraftforge:forge:1.20.1-47.3.0")
    implementation(project(":common"))
}

tasks.shadowJar {
    archiveBaseName.set("cartograph-forge")
    archiveClassifier.set("")
    dependencies {
        include(project(":common"))
    }
}

// Point reobfJar at the shadow JAR so the final artifact is reobfuscated
afterEvaluate {
    val reobfJar = tasks.named("reobfJar", net.minecraftforge.gradle.userdev.tasks.RenameJarInPlace::class)
    reobfJar.configure {
        input.set(tasks.shadowJar.get().archiveFile)
    }
    tasks.shadowJar {
        finalizedBy(reobfJar)
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
