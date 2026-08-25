plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}

val minecraft: String = stonecutter.current.version

version = "${mod.version}+$minecraft"
base {
    archivesName.set("${mod.id}-common")
}

architectury.common(stonecutter.tree.branches.mapNotNull {
    if (stonecutter.current.project !in it) null
    else it.project.prop("loom.platform")
})

loom {
    accessWidenerPath = file("../../src/main/resources/mobends.accesswidener")
}

repositories {
    mavenCentral()
    maven("https://maven.architectury.dev/")
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())

    modApi("dev.architectury:architectury:${mod.dep("architectury")}")

    modCompileOnly("maven.modrinth:armourers-workshop:${mod.dep("armourers_workshop")}")

    api(project(":core"))
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(if (stonecutter.eval(minecraft, ">=1.20.5")) 21 else 17)
    }
}

tasks.processResources {
    properties(listOf("mobends_version.properties"), "mod_version" to mod.version)
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
}

// This is the COMMON node -- shared sources with no mod loader attached. Loom still generates
// runClient/runServer here, and they launch plain vanilla Minecraft with none of the mod present.
//
// That matters more than it sounds, because an unqualified `./gradlew runClient` runs the task in
// EVERY project that declares one. With these left enabled that is four projects: two common
// (vanilla 1.20.1 and vanilla 1.21.1) and two loader. Four Minecraft windows, two of them useless.
//
// Disabled rather than deleted so the task still resolves and reports why.
tasks.matching { it.name == "runClient" || it.name == "runServer" }.configureEach {
    enabled = false
    group = "versioned"
    description = "Disabled: the common node has no mod loader. Use runActiveClient<Loader>."
}
