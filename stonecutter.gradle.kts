plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.9-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}
stonecutter active "1.20.1" /* [SC] DO NOT EDIT */

// A bare `runClient` runs the task in EVERY project that declares one. The common nodes'
// copies are disabled (they have no loader and would launch plain vanilla), but both LOADER
// nodes would still be scheduled — and since each now switches Stonecutter to its own version
// before running, two of them in one build would fight over the active version and launch the
// wrong game. IntelliJ's Gradle panel makes this easy to hit by accident: it groups tasks under
// their `group`, so "neoforge > loom > runClient" looks project-scoped but is not.
gradle.taskGraph.whenReady {
    val scheduled = allTasks.filter { (it.name == "runClient" || it.name == "runServer") && it.enabled }

    if (scheduled.size > 1) {
        throw GradleException(
            "More than one Minecraft run task is scheduled:\n" +
                scheduled.joinToString("\n") { "  ${it.path}" } +
                "\n\nOnly one can run at a time — each needs Stonecutter switched to its own\n" +
                "version, and they would launch into the same run directory.\n\n" +
                "Pick one — both switch the active version for you:\n" +
                "  ./gradlew runClientForge        (Forge 1.20.1)\n" +
                "  ./gradlew runClientNeoforge     (NeoForge 1.21.1)\n"
        )
    }
}

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("buildAndCollect")
}

for (it in stonecutter.tree.branches) {
    if (it.id.isEmpty()) continue
    val loader = it.id.upperCaseFirst()
    stonecutter registerChiseled tasks.register("chiseledBuild$loader", stonecutter.chiseled) {
        group = "project"
        versions { branch, _ -> branch == it.id }
        ofTask("buildAndCollect")
    }
}

// One stable task per loader, registered for EVERY loader node rather than only the active one.
//
// These used to exist only for whichever version Stonecutter was currently on, so the task you
// wanted was missing precisely when you needed it — you had to know about the active version,
// switch by hand, and only then did the task appear. The run tasks switch for themselves now, so
// these always exist and always do the right thing from any starting state.
for (node in stonecutter.tree.nodes) {
    if (node.branch.id.isEmpty()) continue

    val loader = node.branch.id.upperCaseFirst()
    val version = node.metadata.version

    for (type in listOf("Client", "Server")) {
        tasks.register("run$type$loader") {
            group = "project"
            description = "Runs the $loader $version $type, switching the active version if needed"
            dependsOn("${node.hierarchy}:run$type")
        }

        // The old name, kept so existing muscle memory and docs keep working.
        if (node.metadata == stonecutter.current) {
            tasks.register("runActive$type$loader") {
                group = "project"
                description = "Alias for run$type$loader"
                dependsOn("${node.hierarchy}:run$type")
            }
        }
    }
}
