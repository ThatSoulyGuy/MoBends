# Mo' Bends
[![CurseForge Downloads](http://cf.way2muchnoise.eu/1438637.svg)](https://www.curseforge.com/minecraft/mc-mods/mo-bends) [![Mod Versions](http://cf.way2muchnoise.eu/versions/1438637.svg)](https://www.curseforge.com/minecraft/mc-mods/mo-bends)

![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/ThatSoulyGuy/MoBends.svg?style=for-the-badge)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=for-the-badge)](http://makeapullrequest.com)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/ThatSoulyGuy/MoBends.svg?style=for-the-badge)](https://github.com/ThatSoulyGuy/MoBends/pulls)
[![GitHub issues](https://img.shields.io/github/issues-raw/ThatSoulyGuy/MoBends.svg?style=for-the-badge)](https://github.com/ThatSoulyGuy/MoBends/issues)

Mo' Bends gives players and mobs smooth, bendy animations. Arms and legs bend at the elbows and knees. Players get new
animations for walking, sprinting, jumping, sneaking, swimming, climbing, riding, and fighting. Swords and arrows leave a
trail behind them.

This is the modern version of the classic 1.12.2 mod, now for **Minecraft 1.20.1 (Forge)** and **1.21.1 (NeoForge)**.

## What you need
- **Architectury API** is required.
- Mo' Bends is a client mod. You only need it installed on your own side.

## How to use
1. Put the jar in your `mods` folder together with Architectury API.
2. Start the game and press **G** to open the Mo' Bends menu. You can change this key in Controls.
3. In the menu you can:
   - **Animations** - turn animations on or off for each mob, and pick if mobs spin when they attack.
   - **Config** - turn sword trails, arrow trails and other options on or off.
   - **Weapons** - add items from other mods so they count as weapons and get sword animations and trails.
   - **Customize** - change how your own player model looks.

Found a mod that does not work well with Mo' Bends? Please
[open an issue](https://github.com/ThatSoulyGuy/MoBends/issues) and say which mod it is.

## Discord
Want to report a bug, share ideas, or just hang out? Join the Discord server.

[![Discord](https://img.shields.io/discord/386940930739011584.svg?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/JqgWRgdkvx)

## Addons
Other mods can add their own animated mobs to Mo' Bends. Implement `IAddon` and register it with
`Addons.registerAddon(modId, addon)` from the `goblinbob.mobends.api.addon` package. The `DefaultAddon` class in
`src/main/java/goblinbob/mobends/standard` shows how the built-in mobs are registered.

## License
Mo' Bends is released under the [MIT License](LICENSE).
Original mod by GoblinBob (Iwo Plaza). Modern versions by ThatSoulyGuy and Sxilverrr.
