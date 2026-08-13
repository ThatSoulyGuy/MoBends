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
    neoForge()
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
    get("developmentNeoForge").extendsFrom(commonBundle)
}

loom {
    accessWidenerPath = file("../../../src/main/resources/mobends.accesswidener")
    runConfigs.all {
        isIdeConfigGenerated = true
        runDir = "../../../run"
    }
}

repositories {
    mavenCentral()
    maven("https://maven.architectury.dev/")
    maven("https://maven.neoforged.net/releases/")
    maven("https://cursemaven.com") { content { includeGroup("curse.maven") } }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())
    "neoForge"("net.neoforged:neoforge:${common.mod.dep("neoforge_loader")}")

    modApi("dev.architectury:architectury-neoforge:${common.mod.dep("architectury")}")

    modCompileOnly("curse.maven:curios-309927:${common.mod.dep("curios")}")

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionNeoForge")) { isTransitive = false }

    commonBundle(project(":core")) { isTransitive = false }
    shadowBundle(project(":core")) { isTransitive = false }
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.processResources {
    properties(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta"),
        "mod_id" to mod.id,
        "mod_name" to mod.name,
        "mod_version" to mod.version,
        "mod_authors" to mod.prop("authors"),
        "mod_description" to mod.prop("description"),
        "mod_license" to mod.prop("license"),
        "minecraft_version_range" to common.mod.prop("mc_dep_forgelike"),
        "neoforge_version_range" to common.mod.prop("neoforge_range"),
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
