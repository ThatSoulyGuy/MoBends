# Mo' Bends
[![CurseForge Downloads](http://cf.way2muchnoise.eu/1438637.svg)](https://www.curseforge.com/minecraft/mc-mods/mo-bends) [![Mod Versions](http://cf.way2muchnoise.eu/versions/1438637.svg)](https://www.curseforge.com/minecraft/mc-mods/mo-bends)


![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/ThatSoulyGuy/MoBends.svg?style=for-the-badge)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=for-the-badge)](http://makeapullrequest.com)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/ThatSoulyGuy/MoBends.svg?style=for-the-badge)](https://github.com/ThatSoulyGuy/MoBends/pulls)
[![GitHub issues](https://img.shields.io/github/issues-raw/ThatSoulyGuy/MoBends.svg?style=for-the-badge)](https://github.com/ThatSoulyGuy/MoBends/issues)

A Minecraft mod that adds more realistic looking animations to the inhabitants of your blocky world, now in **1.21.1** and **1.20.1**!

## Discord
The development of version **5.0.0** is in progress right now! If you'd like to be a part of it, see the progress, or just hang out, join our Discord server!

[![Discord](https://img.shields.io/discord/386940930739011584.svg?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/JqgWRgdkvx)

Say you came from GitHub if you decide to come by! Hope to see you there

## Local Development Setup
Install a JDK for each target: **JDK 17** for Minecraft 1.20.1 and **JDK 21** for 1.21.1. Gradle itself must run on
JDK 21 or older — the wrapper is Gradle 8.11, which does not support newer JDKs, so set `JAVA_HOME` accordingly if your
system default is newer. I personally use the [Eclipse Temurin JDK](https://adoptium.net/temurin/releases?version=8&os=any&arch=any).

The project builds one source tree into two jars along two axes: **Stonecutter** handles the Minecraft version
(1.20.1 / 1.21.1) and **Architectury** handles the mod loader (Forge / NeoForge). The `core` module is plain Java with
no Minecraft dependency, and is the only module with unit tests.

### Running the game
```bash
./gradlew runClientNeoforge     # NeoForge 1.21.1
./gradlew runClientForge        # Forge 1.20.1
```

There are matching `runServerNeoforge` / `runServerForge`. These always exist and work from any starting state.
Only the active Stonecutter version has materialized sources, and the active version has to be set before Gradle
configures the build — so these switch it and then run the game as a second, nested invocation.

> **Run one at a time, and name it.** A bare `./gradlew runClient` runs the task in *every* project that declares
> one, which is both loaders at once — they need different active versions and share a single `run/` directory, so
> the build stops and tells you to pick. Watch for this in IntelliJ's Gradle panel especially: it groups tasks by
> their `group`, so `neoforge > loom > runClient` looks project-scoped but is not.

### Building and testing
```bash
./gradlew :core:test chiseledBuild
```

`chiseledBuild` is the only command that verifies both loaders — it preprocesses each version in its own cache without
touching your working tree, and drops the jars in `build/libs/<version>/<loader>/`.

> **Do not use a per-node build as a check.** `./gradlew :neoforge:1.21.1:build` while 1.20.1 is active prints
> `BUILD SUCCESSFUL` having compiled nothing, and writes a jar containing only the `core` classes — no mod metadata and
> no entrypoint. Every task on the non-active version is `NO-SOURCE`. CI verifies jar contents for this reason.

### Before committing
Run `./gradlew "Reset active project"`. Switching the active version rewrites the handful of files carrying Stonecutter
`//?` comments in place, and a version-toggle diff mixed into a real change is unreviewable.

### Troubleshooting
- IntelliJ freaks out and can't find symbols in the project, but compiles fine.
    - `File > Invalidate Caches/Restart` works like a charm <3
- Crashing on a NullPointerException inside FML.
    - Click "Download Sources" in the Gradle sidebar
- `Invalid paths argument, contained no existing paths` when launching.
    - You launched a per-node `runClient` directly. Use `runClientNeoforge` / `runClientForge`, which switch for you.
- `java.lang.module.ResolutionException: ... contains package ...` when launching.
    - A package exists in both `core` and `src/main`. The shipped jar merges them so it builds fine, but the dev run
      keeps them separate and rejects the overlap. `./gradlew :core:checkNoSplitPackages` names the offender.

## Creating addons
Addons are a way to extend the functionality of Mo' Bends, e.g. adding support for new mobs.

There's an example addon that I set up a while back, but the sources for CustomNPCs are
no longer available, which makes it impossible to use. I'm looking for a replacement soon.

[CustomNPCs Support Addon](https://github.com/mobends/mobends-addon-customnpcs)
