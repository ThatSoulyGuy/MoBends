plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.9-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}
stonecutter active "1.21.1" /* [SC] DO NOT EDIT */

// A task that lives in a version node sets the active version to its own before it runs. Asking
// for `:neoforge:1.21.1:runClient` while 1.20.1 is active — which is what IntelliJ sends when you
// run `runClient` under neoforge > 1.21.1 in its Gradle panel — switches to 1.21.1 and runs it.
//
// It cannot happen inside the invocation that was asked for: Stonecutter gives sources only to
// the active version and decides that while CONFIGURING, before any task executes. So the request
// is replaced, here, before Gradle selects tasks, by one GradleBuild task that switches and then
// repeats the original request in a nested build — which configures against the version the
// switch just wrote. It is the mechanism behind run<Type><Loader> below, applied to any task.
//
// Left alone on purpose:
//  - requests that name no version node (chiseledBuild, :core:test, a bare `build` from the root);
//  - requests that span more than one version, which no single active version can satisfy
//    (IntelliJ's "Build Project" is one: it asks every module for `classes`);
//  - IDE sync, and tasks that never read the mod's sources (reports, `clean`, Loom's genSources,
//    IntelliJ's own helper tasks).
//
// Two limits. A project path is matched by full segment names or unique prefixes (`:neo:1.21.1`),
// not by Gradle's camel-case abbreviations. And IntelliJ runs an Application configuration, such
// as Loom's "Minecraft Client (:neoforge:1.21.1)", through a task that exists only in the build
// IntelliJ itself started: that one is switched for and then has to be started a second time.
val relaunchedProperty = "mobends.stonecutter.relaunched"

run {
    if (System.getProperty("idea.sync.active", "false").toBoolean()) return@run

    val start = gradle.startParameter
    val nodes = stonecutter.tree.nodes
    val sourceless = setOf(
        "help", "tasks", "projects", "properties", "dependencies", "dependencyInsight",
        "buildEnvironment", "outgoingVariants", "resolvableConfigurations", "javaToolchains",
        "components", "model", "dependentComponents", "clean"
    )
    // Task options that take no value. Any other `--option` without `=` is followed by one.
    val flags = setOf("--debug-jvm", "--rerun", "--fail-fast", "--all")

    // Gradle resolves a project path one segment at a time, by exact name or by abbreviation.
    fun child(parent: Project, segment: String): Project? =
        parent.childProjects[segment]
            ?: parent.childProjects.entries.singleOrNull { it.key.startsWith(segment) }?.value

    fun resolve(from: Project, path: String): Project? =
        path.split(':').filter { it.isNotEmpty() }.fold(from as Project?) { project, segment ->
            project?.let { child(it, segment) }
        }

    // An unqualified name runs in the project of the directory Gradle was started in and in every
    // project below it: a terminal inside neoforge/ or neoforge/versions/1.21.1, or IntelliJ's
    // Run Anything with a module selected. From the root that is every node, so it names none.
    val currentDir = start.currentDir.canonicalFile
    val here = allprojects.firstOrNull { it.projectDir.canonicalFile == currentDir } ?: rootProject

    fun nodesUnder(project: Project) =
        if (project == rootProject) emptyList()
        else nodes.filter { node -> generateSequence(node.project) { it.parent }.any { it == project } }

    val targets = mutableSetOf<String>()
    for (request in start.taskRequests) {
        val requestPath = request.projectPath
        val base = if (requestPath != null) findProject(requestPath) ?: continue else here
        var optionValue = false

        for (arg in request.args) {
            // `--tests Foo`, `--args "..."`: the token after such an option is its value, not a task.
            if (arg.startsWith("-")) {
                optionValue = arg.startsWith("--") && "=" !in arg && !arg.startsWith("--no-") && arg !in flags
                continue
            }
            if (optionValue) { optionValue = false; continue }

            val name = arg.substringAfterLast(':')
            val owners = if (':' in arg) {
                val project = resolve(if (arg.startsWith(":")) rootProject else base, arg.substringBeforeLast(':'))
                nodes.filter { it.project == project }
            } else nodesUnder(base)

            val readsNoSources = name in sourceless || name.startsWith("genSources") ||
                (name.startsWith("ij") && name.getOrNull(2)?.isUpperCase() == true)

            for (node in owners) {
                // The common nodes' run tasks are disabled; see build.gradle.kts.
                val disabledRun = node.branch.id.isEmpty() && (name == "runClient" || name == "runServer")
                if (!readsNoSources && !disabledRun) targets += node.metadata.project
            }
        }
    }

    val target = targets.singleOrNull() ?: return@run
    val active = stonecutter.current.project
    if (target == active) return@run

    if (providers.gradleProperty(relaunchedProperty).isPresent) {
        throw GradleException(
            "Stonecutter is still on $active after switching to $target.\n" +
                "Run `./gradlew \"Set active project to $target\"` by itself and check its output."
        )
    }

    if (start.isConfigureOnDemand) {
        throw GradleException(
            "Cannot switch Stonecutter to $target for this request while configure-on-demand is on:\n" +
                "Gradle has already read the task list. Run `./gradlew \"Set active project to $target\"` first."
        )
    }

    // Read now: the tasks below are configured lazily, after the request has been replaced.
    val originalRequests = start.taskRequests.toList()
    val requested = start.taskNames.joinToString(" ")
    val excludedTasks = start.excludedTaskNames.toSet()
    val projectProperties = start.projectProperties.toMap()
    val initScripts = start.initScripts.toList()
    val switchTask = "Set active project to $target"

    val replacement = if (start.taskNames.any { it.substringAfterLast(':').endsWith(".main()") }) {
        // IntelliJ's Application run task. Its init script adds it to one exact project of the
        // build IntelliJ started, so no nested build can contain it: switch, then ask for a re-run.
        tasks.register("switchToOwnVersion") {
            description = "Sets the active version to $target for a run configuration started from the IDE"
            dependsOn(switchTask)
            doLast {
                throw GradleException(
                    "Stonecutter was on $active and is now on $target. Start it again.\n" +
                        "The IDE creates this run task itself, so it could not be repeated after the switch."
                )
            }
        }
    } else {
        tasks.register<GradleBuild>("runWithOwnVersionActive") {
            description = "Sets the active version to $target, then runs: $requested"
            dependsOn(switchTask)
            buildName = "own-version"
            dir = currentDir
            startParameter.setTaskRequests(originalRequests)
            startParameter.setExcludedTaskNames(excludedTasks)
            startParameter.setInitScripts(initScripts)
            startParameter.projectProperties = projectProperties + (relaunchedProperty to target)
        }
    }

    logger.lifecycle(
        if (start.isDryRun) "Stonecutter is on $active; would switch to $target to run $requested"
        else "Stonecutter is on $active; switching to $target to run $requested"
    )
    if (System.getProperty("idea.debugger.dispatch.port") != null) {
        logger.lifecycle("If the debugger does not attach to this run, start Debug again now that $target is active.")
    }

    start.setTaskNames(listOf(":${replacement.name}"))

    // Give the real request back once Gradle has picked its tasks. A continuous build reads it
    // again for its next round, and the IDE reads it to find the task it is running or debugging.
    gradle.taskGraph.whenReady { start.setTaskRequests(originalRequests) }
}

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
        // A nested build, not a plain dependsOn. Stonecutter decides which nodes have sources at
        // CONFIGURATION time, and Gradle configures everything before executing anything — so a
        // switch performed during this build cannot give this node sources. GradleBuild starts a
        // fresh invocation, which configures against the version the switch just wrote.
        tasks.register<GradleBuild>("run$type$loader") {
            group = "project"
            description = "Runs the $loader $version $type, switching the active version if needed"
            dependsOn("Set active project to $version")
            dir = rootDir
            tasks = listOf("${node.hierarchy}:run$type")
        }
    }
}
