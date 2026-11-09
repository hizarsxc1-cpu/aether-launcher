# AETHER TOUCH MACRO

Target: Minecraft Java 1.21.0 / Fabric / Java 21 / PojavLauncher.

## Hotbar
1 Sword | 2 Obsidian | 3 End Crystal | 8 Glowstone | 9 Respawn Anchor.

## Touchscreen controls in Pojav
- Z = ANCHOR: place Anchor, charge with Glowstone, equip a Totem to offhand if found in inventory, then attempt detonation.
- X = CRYSTAL: place Obsidian, then End Crystal.

Pojav menu > Custom controls > gear:
1. Add Button named ANCHOR and assign Z.
2. Add Button named CRYSTAL and assign X.
3. Position, resize, set opacity, and save.

## Important behavior notes
- The macro sends the interaction sequence without an intentional tick delay. Minecraft/server still processes packets and world updates; zero latency cannot be guaranteed.
- Totem search checks the player's 36 main-inventory slots and offhand. A found Totem is moved to offhand. If none is found, the macro selects Sword slot 1 and still sends the detonation interaction.
- This is a client-side macro. Server rules, ping, protection plugins, anti-cheat, target block, item counts, placement space, and server-side update order can cause an action to fail.
- The macro does not guarantee a successful explosion. Respawn Anchors explode when activated in dimensions where they are not valid respawn anchors (such as the Overworld); in the Nether they are used for respawning instead. Test in a private world and use only where server rules allow macros.
- Crystal macro places Obsidian and End Crystal; destroying the Crystal remains manual.

## Build
Open GitHub Actions > Build AETHER Touch Macro > Run workflow.
Download artifact AETHER-Touch-Macro-MC-1.21.0; extract its JAR and put it in .minecraft/mods.

This folder is an isolated Fabric mod project inside the Aether Launcher repository; it does not replace or modify the Android launcher Gradle project.
