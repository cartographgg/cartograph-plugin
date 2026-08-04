plugins {
    `java-library`
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
    implementation(project(":common"))
    compileOnly(libs.velocity.v121.api)
    compileOnly(libs.snakeyaml)
    annotationProcessor(libs.velocity.v121.api)
}

// Velocity's annotation processor bakes the plugin version into
// velocity-plugin.json from the @Plugin annotation at compile time, so the
// version must be a compile-time constant — resource ${version} expansion can't
// reach it. Generate a BuildConstants.VERSION constant from the Gradle project
// version and reference it from @Plugin (see CartographVelocityPlugin).
val generatedConstantsDir = layout.buildDirectory.dir("generated/sources/buildConstants/java/main")

val generateBuildConstants by tasks.registering {
    val outputDir = generatedConstantsDir
    val projectVersion = version.toString()
    inputs.property("version", projectVersion)
    outputs.dir(outputDir)
    doLast {
        val pkgDir = outputDir.get().dir("gg/cartograph/plugin/velocity").asFile
        pkgDir.mkdirs()
        pkgDir.resolve("BuildConstants.java").writeText(
            """
            package gg.cartograph.plugin.velocity;

            /** Generated at build time from the Gradle project version — do not edit. */
            public final class BuildConstants {
                public static final String VERSION = "$projectVersion";

                private BuildConstants() {
                }
            }
            """.trimIndent() + "\n"
        )
    }
}

sourceSets.named("main") {
    java.srcDir(generatedConstantsDir)
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(generateBuildConstants)
}
