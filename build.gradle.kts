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
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())

    modApi("dev.architectury:architectury:${mod.dep("architectury")}")

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
