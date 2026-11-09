# AETHER Touch Macro (Fabric 1.21.0)

Standalone Fabric client mod inside the existing Aether Launcher repository.

## Touch buttons
- Z = Anchor Macro: slot 9 Respawn Anchor -> slot 8 Glowstone -> Totem if available -> activate charged Anchor.
- X = Crystal Macro: slot 2 Obsidian -> slot 3 End Crystal.
- Anchor uses Sword slot 1 if no Totem is available. The absence of a Totem never cancels the detonation attempt.
- Totem can be in hotbar, offhand, or main inventory. A Totem found in main inventory is swapped into offhand.
- No artificial fixed delay is inserted between actions. Anchor placement and charging still wait for the actual client/server block-state confirmation, which is required for reliable sequencing.

## Pojav touchscreen setup
Open Pojav side menu -> Custom controls -> gear:
1. Add button ANCHOR and map it to Z.
2. Add button CRYSTAL and map it to X.
3. Place and resize the buttons, then save.

## Hotbar layout
1 Sword | 2 Obsidian | 3 End Crystal | 8 Glowstone | 9 Respawn Anchor.

## Notes
Anchor explosion depends on dimension rules: in the Nether, Respawn Anchors are valid respawn blocks and do not explode. Server latency, protection plugins, anti-cheat, placement space, and server rules can still reject a macro interaction. Test in single-player first and use only where server rules permit macros. Crystal destruction remains manual.

Build with Java 21 and Gradle 8.8 from this directory. The existing workflow in .github/workflows/h-fastanchor.yml builds the JAR on branch h-fastanchor-mod.
