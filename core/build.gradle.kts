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
    description = "Fails if a package exists in both :core and the common mod sources"

    val coreRoot = file("src/main/java")
    val commonRoot = rootProject.file("src/main/java")
    inputs.dir(coreRoot).withPathSensitivity(PathSensitivity.RELATIVE)
    if (commonRoot.isDirectory) {
        inputs.dir(commonRoot).withPathSensitivity(PathSensitivity.RELATIVE)
    }
    outputs.upToDateWhen { true }

    doLast {
        fun packagesIn(root: File): Set<String> =
            if (!root.isDirectory) emptySet()
            else root.walkTopDown()
                .filter { it.isFile && it.extension == "java" }
                .map { it.parentFile.relativeTo(root).path.replace(File.separatorChar, '.') }
                .toSet()

        val shared = packagesIn(coreRoot) intersect packagesIn(commonRoot)
        if (shared.isNotEmpty()) {
            throw GradleException(
                "Split package(s) between :core and the common mod sources:\n" +
                    shared.sorted().joinToString("\n") { "  $it" } +
                    "\n\nEach package must live in exactly one module. This builds a working jar " +
                    "(shadowJar merges the two) but crashes the NeoForge dev run with a module " +
                    "ResolutionException. Move the offending class to a package owned by one module."
            )
        }
    }
}

tasks.check {
    dependsOn(checkNoSplitPackages)
}
