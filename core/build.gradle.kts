plugins {
    `java-library`
}

base {
    archivesName.set("${mod.id}-core")
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.code.gson:gson:2.10.1")
    api("com.google.code.findbugs:jsr305:3.0.2")
    api("org.slf4j:slf4j-api:2.0.9")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}

/**
 * Fails if any Java package exists in BOTH this module and the common mod sources.
 *
 * A split package survives `chiseledBuild` unnoticed, because shadowJar merges core and common
 * into a single jar. It only fails in a dev run, where the two are separate classpath entries:
 * NeoForge's module layer rejects two modules containing the same package with
 * `java.lang.module.ResolutionException`, before the game window ever opens. That is an
 * expensive way to find out, so check it at build time instead.
 *
 * To fix a hit: move the offending class into a package owned by exactly one of the two modules.
 * Registry keys are strings, so relocating a registered class does not change any JSON.
 */
val checkNoSplitPackages by tasks.registering {
    group = "verification"
    description = "Fails if a package exists in two source roots that share a dev-run module layer"

    // Every pair here co-exists on one classpath at runtime: :core is bundled into BOTH loader
    // jars alongside the common sources. forge and neoforge are deliberately absent from each
    // other's pairing -- they never load together, and they legitimately share three mixin
    // packages by design.
    val coreRoot = file("src/main/java")
    val roots = mapOf(
        "the common mod sources" to rootProject.file("src/main/java"),
        "the Forge sources" to rootProject.file("forge/src/main/java"),
        "the NeoForge sources" to rootProject.file("neoforge/src/main/java"),
    )

    inputs.dir(coreRoot).withPathSensitivity(PathSensitivity.RELATIVE)
    roots.values.filter { it.isDirectory }.forEach {
        inputs.dir(it).withPathSensitivity(PathSensitivity.RELATIVE)
    }
    outputs.upToDateWhen { true }

    doLast {
        fun packagesIn(root: File): Set<String> =
            if (!root.isDirectory) emptySet()
            else root.walkTopDown()
                .filter { it.isFile && it.extension == "java" }
                .map { it.parentFile.relativeTo(root).path.replace(File.separatorChar, '.') }
                .toSet()

        val corePackages = packagesIn(coreRoot)
        val offenders = roots.mapValues { (_, root) -> corePackages intersect packagesIn(root) }
            .filterValues { it.isNotEmpty() }

        if (offenders.isNotEmpty()) {
            throw GradleException(
                offenders.entries.joinToString("\n") { (label, shared) ->
                    "Split package(s) between :core and $label:\n" +
                        shared.sorted().joinToString("\n") { "  $it" }
                } +
                    "\n\nEach package must live in exactly one module. A split builds a working jar " +
                    "(shadowJar merges the modules) but crashes the NeoForge dev run before the " +
                    "window opens, with a module ResolutionException. Move the offending class to " +
                    "a package owned by exactly one module."
            )
        }
    }
}

// Hung off compileJava, not just check: chiseledBuild, a per-node build and runClient all reach
// compileJava, and none of them reach check -- so wiring this to check alone would mean the guard
// never ran on any command that can actually reintroduce the bug.
tasks.compileJava {
    dependsOn(checkNoSplitPackages)
}

tasks.check {
    dependsOn(checkNoSplitPackages)
}
