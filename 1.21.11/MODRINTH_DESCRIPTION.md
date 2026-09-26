# Combat Audio Mixer

Tune the sounds that matter during Crystal PvP and other fast fights without replacing Minecraft's audio system.

**Combat Audio Mixer** is a lightweight, client-side Fabric mod for **Minecraft Java 1.21.11**. It gives you separate live volume controls for combat-relevant sounds, quick presets for different play styles, and local preset sharing. Your settings stay on your computer, so the mod does not require a server plugin, account, or hosted service.

## What it does

- Separates 27 mapped vanilla sound events into eight practical groups.
- Applies each group’s gain live while you play, from **0% (muted) to 150%**.
- Includes individual previews and a short overlapping combat mix so you can tune levels before joining a fight.
- Provides **Neutral**, **Focus**, and **Quiet** starting presets.
- Lets you save, export, copy, import, and drag-and-drop portable JSON presets.
- Offers a temporary **Configured mix / Original audio** comparison without changing your saved settings.
- Updates sounds that are already playing when you change a level.
- Keeps Minecraft’s original pitch, position, attenuation, hearing radius, master volume, and category mute behavior intact.
- Adds a **Mixer ON/OFF** switch and a reset button that returns group levels to 100%.

## Combat groups

The mixer covers crystals and other generic explosions, anchors, totems, pearls, melee hits, player hurt sounds, eating and drinking, equipment, blocks, and other documented events. Hover a slider in-game to see its exact event coverage.

Minecraft uses shared sound events for some effects. For example, the explosion control also affects anchors, TNT, and any other source that uses Minecraft’s generic explosion event. The mod does not try to guess whether a sound came from an opponent, an ally, or a hidden entity.

## Open the mixer

- **Options → Music & Sound → Combat Audio Mixer…**
- Press **F8** while in a world.
- Use the client-side **`/combataudio`** command.

The F8 binding can be changed under **Options → Controls → Key Binds → Combat Audio Mixer**. An optional bypass shortcut starts unbound so it will not take over an existing key.

## Presets and files

Open the **Presets** screen to cycle through the built-in examples or create your own. A preset can be exported as JSON, copied to the clipboard, imported from the clipboard, or loaded by dropping one JSON file onto the screen.

Files are stored in the selected Minecraft instance:

```text
config/combat-audio-mixer/config.json
config/combat-audio-mixer/presets/<name>.json
```

Preset names are limited to safe ASCII characters and imports are size- and schema-checked. Unknown groups are skipped with a visible notice; omitted groups keep the neutral 100% level. Saving replaces files safely and keeps a backup when possible.

## Installation

1. Create or select a **Minecraft 1.21.11 Fabric** instance.
2. Install **Fabric Loader 0.18.4 or newer**, **Fabric API for 1.21.11**, and Java 21 or newer.
3. Put `combat-audio-mixer-1.0.0+mc1.21.11.jar` in that instance’s `mods` folder.
4. Launch the instance and open the mixer using one of the controls above.

This is a client-side mod. It does not need to be installed on a server and does not change server state. Lunar Client can use it only when the selected Lunar profile supports loading Fabric 1.21.11 mods; follow Lunar’s current Fabric-mod installation rules.

## Compatibility and scope

The mod changes the gain of supported local sound sources. It does not change gameplay, packets, hit registration, reach, timing, sound distance, or other players’ audio. Resource packs still control the actual sound samples. Events introduced by other mods are outside this release’s mapping.

The 150% per-source ceiling is a gain limit, not a compressor or combined-output limiter. Several loud sounds played at once can still be loud, so choose levels that are comfortable for your headphones or speakers.

## Requirements

- Minecraft Java **1.21.11**
- Fabric Loader **0.18.4+**
- Fabric API for **1.21.11**
- Java **21+**

Combat Audio Mixer is released under the MIT License.
