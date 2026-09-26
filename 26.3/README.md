# Combat Audio Mixer

A client-side customizable sound mod for **Minecraft Java 26.3 + Fabric**.

Eight combat sound groups, live 0-150% volume controls, individual and overlapping previews, neutral/focus/quiet presets, and local JSON preset sharing. It does not require a Minecraft server plugin or a hosted service.

## Installation

1. Use a Minecraft **26.3** instance with **Fabric Loader 0.19.5 or newer**.
2. Put `combat-audio-mixer-1.0.0+mc26.3.jar` in that instance's `mods` folder.
3. Install Fabric API for 26.3 in the same folder — download it from https://fabricmc.net/use/installer/ or the Fabric API releases page. Keep only one compatible Fabric API version in the instance.
4. Start Minecraft. Open **Options -> Music & Sound -> Combat Audio Mixer...**. In a world, **F8** or the client command **/combataudio** also opens it.

This release targets 26.3 exactly. The Minecraft runtime needs Java 25 or newer. Testing used Java 25. The mod does not install into your existing game automatically.

## Controls

- Each slider controls an explicitly documented group. **100%** is unchanged, **0%** mutes the group, and **150%** is the upper limit. Minecraft's master/category muting remains respected.
- **Preview** plays a modest-level sample in the relevant vanilla sound category.
- **Preview mix** plays a short sequence with two overlapping explosions, a totem, food, an anchor charge, and a pearl throw.
- **Stop** stops only samples started by this mod's preview controls.
- **Mixer: ON/OFF** persistently enables or disables all adjustments.
- **Configured mix / Original audio** toggles a temporary comparison bypass. Bypass is not saved across restarts.
- **Reset levels** returns all groups to 100% while retaining the enabled/disabled state.
- Settings apply live. Saving is debounced by about one second and also happens when the screens close. Read/write errors appear in the status line and client log.
- Controls -> Key Binds -> Combat Audio Mixer lets you change F8 or assign the optional bypass shortcut, which starts unbound.

Hover a slider for the exact sound-event coverage. On smaller windows, use the page arrows to reach the remaining groups.

## Presets

The Presets screen includes **Neutral**, **Focus**, and **Quiet** examples. They are starting points, not a universally optimal competitive mix.

Use the arrows or click the preset name to cycle, then select **Apply**. Enter a name and use **Save / export file** to create a portable JSON file. Saving the same name keeps the previous file as `.bak`.

You can also **Copy preset JSON**, **Import clipboard**, or drop exactly one JSON file onto the Presets screen. Imported known groups apply immediately. Unrecognized groups produce a visible notice and are skipped; omitted groups default to 100%. Importing does not create a named preset file until you choose Save / export.

Local files live under your selected Minecraft instance:

```text
config/combat-audio-mixer/config.json
config/combat-audio-mixer/presets/<name>.json
```

Preset names accept 1-40 ASCII letters, digits, spaces, underscores, and hyphens, starting with a letter or digit. Imports are limited to 64 KiB. Unknown schema versions and invalid/out-of-range gains are rejected. Invalid configuration is preserved under a separate filename before neutral defaults are loaded. Normal replacement writes use a temporary file, atomic replacement where supported, and a backup.

## Sound coverage

See [SUPPORTED-SOUNDS.md](SUPPORTED-SOUNDS.md) for the complete 27-event list.

The explosion group uses Minecraft's shared generic explosion event. It also affects anchors, TNT, and any other source using that event. Stone/glass placement controls also affect other blocks using those same sounds. The mod does not infer whether a sound came from your opponent or from a hidden entity.

The gain hook leaves the original pitch, position, hearing radius, and attenuation mode intact. It modifies source gain after the original volume has already been used for Minecraft's hearing-distance calculation. Live updates refresh existing sound sources; a sound skipped because it began at zero volume is not retroactively replayed when unmuted.

The 150% ceiling and per-source clamp are **not** a compressor or a combined-output loudness limiter. Many overlapping sounds can still be loud. Resource packs retain control of the actual samples for the supported event IDs. Different event IDs introduced by another mod are outside this version's mapping.

## Build and development

Pinned versions: Minecraft 26.3, official Mojang mappings (game unobfuscated since 26.1), Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Fabric Loom 1.17.21, Gradle 9.5.1.

Use JDK 25 or a compatible newer JDK. Gradle 9.5.1 and JDK 25 were used for the delivered build. The compiled Java classes target Java 25. The wrapper verifies the Gradle distribution checksum.

Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat runClient
```

Linux/macOS:

```sh
sh ./gradlew build
sh ./gradlew test
sh ./gradlew runClient
```

The installable artifact is the ordinary JAR under `build/libs`, without `-sources` in its name. Do not install a sources JAR. Dependencies and Minecraft development assets are downloaded on the first build; no Minecraft game binaries are included in this source archive.

Opt-in isolated client checks:

```powershell
.\gradlew.bat runClient -PsmokeTest
```

This development run opens an isolated client, plays low-level diagnostic sounds, inspects OpenAL source state, captures the screens, and shuts down. It uses the project's `run` directory, never an existing launcher instance. Results are in `run/smoke-results.txt`, with PNGs under `run/screenshots`.

For the resource-pack test, copy `src/test/resources/resourcepack` to `run/resourcepacks/CombatAudioSmoke`, enable `file/CombatAudioSmoke` in the isolated run's resource-pack list, then run `runClient -PsmokeTest -PsmokePack`. This fixture substitutes the vanilla experience-orb sample for the explosion event and verifies that the mapping still controls the event. The fixture is not part of the release mod's resources.

## Code map

- `core`: immutable settings, gains, groups, and built-in presets.
- `config`: versioned JSON parsing, bounded imports, atomic writes, backups.
- `audio`: local preview scheduling and source tracking.
- `ui`: responsive mixer and preset screens.
- `mixin`: initial/live source-gain hooks, Sound Options entry, and diagnostic accessors.
- `validation`: explicitly opt-in development-client smoke test.
- `src/test`: 25 unit tests and an original resource-pack test manifest.

## Validation and limits

See [VALIDATION.md](VALIDATION.md) for the checks actually performed. The first release has not been evaluated for subjective listening comfort or compatibility with every third-party audio mod, custom client, and server policy. All game tests used a separate development client; no public-server fight or multiplayer anti-cheat certification is claimed.

## License and dependencies

Combat Audio Mixer source is MIT licensed. Fabric API is a separate upstream dependency distributed under Apache-2.0; its original license is included with the installation bundle. The Gradle wrapper is upstream Gradle code under Apache-2.0. Minecraft, its assets, Fabric, and Gradle retain their respective ownership and licenses.

Primary references:
- Fabric 26.3 development documentation: https://docs.fabricmc.net/develop/
- Official Mojang mappings (game unobfuscated since 26.1); client sound API: `net.minecraft.client.sounds.SoundEngine`
- Fabric API: https://github.com/FabricMC/fabric
- Gradle wrapper documentation: https://docs.gradle.org/current/userguide/gradle_wrapper.html
