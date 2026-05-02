plugins {
    alias(libs.plugins.shadow) apply false
}

allprojects {
    group = property("group") as String
    version = property("version") as String
}

val testrigPathProvider = providers.gradleProperty("testrigPath")
    .orElse("../cartograph-testrig")

// Maps subproject path → (platform, line, typeSubdir).
// For bukkit, each platform variant (paper/spigot/folia) gets its own
// subdirectory so that only one JAR is present when the server starts,
// avoiding Cartograph's ambiguous-plugin-name conflict on Paper/Folia.
// Non-bukkit platforms don't have this issue (only one JAR per directory).
data class TestrigDest(val platform: String, val line: String, val type: String = "")

// Folia v26_1 and NeoForge v26_1 omitted — Folia has no 26.x release upstream
// and NeoForge 26.x is beta-only.
val testrigPlatformMap = mapOf(
    ":bukkit:paper:v1_21"     to TestrigDest("bukkit",     "v1_21", "paper"),
    ":bukkit:paper:v26_1"     to TestrigDest("bukkit",     "v26_1", "paper"),
    ":bukkit:spigot:v1_21"    to TestrigDest("bukkit",     "v1_21", "spigot"),
    ":bukkit:spigot:v26_1"    to TestrigDest("bukkit",     "v26_1", "spigot"),
    ":bukkit:folia:v1_21"     to TestrigDest("bukkit",     "v1_21", "folia"),
    ":bungeecord:v1_21"       to TestrigDest("bungeecord", "v1_21"),
    ":bungeecord:v26_1"       to TestrigDest("bungeecord", "v26_1"),
    ":velocity:v1_21"         to TestrigDest("velocity",   "v1_21"),
    ":velocity:v26_1"         to TestrigDest("velocity",   "v26_1"),
    ":neoforge:v1_21"         to TestrigDest("neoforge",   "v1_21"),
)

tasks.register("installToTestrig") {
    group = "distribution"
    description = "Copies built plugin JARs into the cartograph-testrig plugins/ directory."

    testrigPlatformMap.keys.forEach { path -> dependsOn("$path:build") }

    doLast {
        val targetRoot = file(testrigPathProvider.get())
        if (!targetRoot.exists()) {
            throw GradleException(
                "Testrig path does not exist: $targetRoot. " +
                "Pass -PtestrigPath=<path> or create the directory."
            )
        }

        testrigPlatformMap.forEach { (subprojectPath, dest) ->
            val sub = project(subprojectPath)
            val libsDir = sub.layout.buildDirectory.dir("libs").get().asFile
            val jars = libsDir.listFiles { f ->
                f.isFile && f.name.startsWith("cartograph-") && f.name.endsWith(".jar")
            } ?: emptyArray()
            if (jars.isEmpty()) {
                logger.warn("no JAR in $libsDir; did $subprojectPath:build run?")
                return@forEach
            }
            val relPath = if (dest.type.isNotEmpty()) {
                "plugins/${dest.platform}/${dest.line}/${dest.type}"
            } else {
                "plugins/${dest.platform}/${dest.line}"
            }
            val targetDir = File(targetRoot, relPath)
            if (!targetDir.exists()) {
                throw GradleException(
                    "Testrig plugin destination missing: $targetDir. " +
                    "Run the testrig's directory-skeleton step first."
                )
            }
            jars.forEach { jar ->
                val out = File(targetDir, jar.name)
                jar.copyTo(out, overwrite = true)
                logger.lifecycle("copied ${jar.name} → $targetDir")
            }
        }
    }
}
