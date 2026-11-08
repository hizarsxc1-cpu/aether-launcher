# H FastAnchor (Fabric 1.21)

Standalone client mod project nested under `mods/h-fastanchor` so it does not alter Aether Launcher app sources.

Default key: **V**. Finds Respawn Anchor, Glowstone and Totem in any of the 9 hotbar slots, places an Anchor on the block face under the crosshair, uses Glowstone once, selects Totem, then sends the final Anchor interaction. Offhand and inventory arrangement are left unchanged.

## Build
Requires Java 21 and Gradle 8.8:
`gradle build`

The output JAR is in `build/libs/`.

## Prototype warning
This is an experimental client-side sequence, not a verified release. Server latency, dimension rules, block placement geometry, interaction range and server policies affect the result. It does not bypass anti-cheat; some servers may prohibit automated interactions. Test in a private world first.
