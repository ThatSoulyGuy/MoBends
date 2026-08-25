@file:Suppress("UnstableApiUsage")

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.github.johnrengelman.shadow")
}

val loader = prop("loom.platform")!!
val minecraft: String = stonecutter.current.version
val common: Project = requireNotNull(stonecutter.node.sibling("")?.project) {
    "No common project for $project"
}

version = "$minecraft-${mod.version}-$loader"
base {
    archivesName.set(mod.id)
}

architectury {
    platformSetupLoomIde()
    forge()
}

val commonBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val shadowBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

configurations {
    compileClasspath.get().extendsFrom(commonBundle)
    runtimeClasspath.get().extendsFrom(commonBundle)
    get("developmentForge").extendsFrom(commonBundle)
}

loom {
    accessWidenerPath = file("../../src/main/resources/mobends-forge.accesswidener")
    forge {
        convertAccessWideners = true
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
        mixinConfig("mobends-forge.mixins.json", "mobends-forge-armourers.mixins.json", "mobends-forge-carryon.mixins.json")
    }
    mixin {
        defaultRefmapName = "mobends-forge.refmap.json"
    }
    runConfigs.all {
        isIdeConfigGenerated = true
        runDir = "../../../run"
    }
}

repositories {
    mavenCentral()
    maven("https://maven.architectury.dev/")
    maven("https://maven.minecraftforge.net")
    maven("https://maven.theillusivec4.top") { content { includeGroup("top.theillusivec4.curios") } }
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())
    "forge"("net.minecraftforge:forge:$minecraft-${common.mod.dep("forge_loader")}")

    modApi("dev.architectury:architectury-forge:${common.mod.dep("architectury")}")

    modCompileOnly("top.theillusivec4.curios:curios-forge:${common.mod.dep("curios")}")

    modCompileOnly("maven.modrinth:armourers-workshop:${common.mod.dep("armourers_workshop")}")

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionForge")) { isTransitive = false }

    commonBundle(project(":core")) { isTransitive = false }
    shadowBundle(project(":core")) { isTransitive = false }
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaExec>().configureEach {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.processResources {
    properties(listOf("META-INF/mods.toml", "pack.mcmeta"),
        "mod_id" to mod.id,
        "mod_name" to mod.name,
        "mod_version" to mod.version,
        "mod_authors" to mod.prop("authors"),
        "mod_description" to mod.prop("description"),
        "mod_license" to mod.prop("license"),
        "minecraft_version_range" to common.mod.prop("mc_dep_forgelike"),
        "forge_version_range" to common.mod.prop("forge_range"),
        "loader_version_range" to common.mod.prop("loader_range"),
        "pack_format" to common.mod.prop("pack_format")
    )
}

tasks.jar {
    archiveClassifier = "dev"
}

tasks.named<Jar>("sourcesJar") {
    val commonSources = common.tasks.named<Jar>("sourcesJar")
    val coreSources = project(":core").tasks.named<Jar>("sourcesJar")
    dependsOn(commonSources, coreSources)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(commonSources.map { zipTree(it.archiveFile) })
    from(coreSources.map { zipTree(it.archiveFile) })
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier = "dev-shadow"
    exclude("fabric.mod.json", "architectury.common.json")
}

tasks.remapJar {
    input = tasks.shadowJar.get().archiveFile
    archiveClassifier = null
    dependsOn(tasks.shadowJar)
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
}

tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
    from(tasks.remapJar.get().archiveFile, tasks.remapSourcesJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}/$loader"))
    dependsOn("build")
}

// Stonecutter decides which nodes have sources at CONFIGURATION time, and Gradle configures every
// project before it executes anything. So switching the active version mid-build is too late: this
// node was already configured with no usable output, and the launch dies with
// "Invalid paths argument, contained no existing paths" before any mod loads.
//
// The switch therefore has to happen in a SEPARATE Gradle invocation, which is what the root
// runClient<Loader> / runServer<Loader> tasks do. Refuse here and point at them.
tasks.matching { it.name == "runClient" || it.name == "runServer" }.configureEach {
    val activeVersion = stonecutter.active.version
    val thisVersion = stonecutter.current.version
    val loaderSuffix = loader.replaceFirstChar { it.uppercaseChar() }
    val runType = if (name == "runClient") "Client" else "Server"

    doFirst {
        if (thisVersion != activeVersion) {
            throw GradleException(
                "Cannot run $thisVersion while Stonecutter's active version is $activeVersion.\n" +
                    "Only the active version has sources, and the active version has to be set\n" +
                    "before Gradle configures the build — so it cannot be switched from inside it.\n\n" +
                    "Use the root task, which does the switch as its own invocation:\n" +
                    "  ./gradlew run$runType$loaderSuffix\n"
            )
        }
    }
}
