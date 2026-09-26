# Release validation: Combat Audio Mixer 1.0.0

## Environment

- Minecraft Java 26.3, official Mojang mappings (game unobfuscated since 26.1).
- Fabric Loader 0.19.5 and Fabric API 0.161.0+26.3.
- Windows; Temurin JDK 25.0.4.1; Java bytecode target 25.
- Fabric Loom 1.17.21; Gradle 9.5.1.
- OpenAL Soft initialized on the available CORSAIR VOID WIRELESS v2 headset output.
- A separate development game directory; no public server connection and no change to an existing Minecraft instance.

## Automated unit checks: 25 passed

9 core tests cover neutral gain, multiplication after vanilla volume adjustment, vanilla/category mute behavior, group mute, the source-gain ceiling, bypass/disable behavior, immutable settings, invalid gains, and unique mappings.

10 preset-codec tests cover round-tripping disabled state and all levels, unsupported and missing schemas, malformed JSON shapes, unknown groups with recognized-value preservation, invalid/nonfinite values, invalid enabled state, mapping-version warnings, input limits, and unsafe filenames.

6 storage tests cover neutral first-run behavior, save/reload/backups, preservation of invalid configuration, portable preset listing and backup exclusion, unsafe name rejection, and oversized files.

## Actual Minecraft/OpenAL integration checks

The test read actual OpenAL source properties from an initialized Minecraft sound engine, not only a standalone gain function. Both the ordinary resource set and a replacement resource pack passed.

- Every mapped event exists in the 26.3 sound registry.
- Initial playback receives the configured explosion-group gain.
- Source pitch, 3D position, and maximum attenuation distance remain unchanged.
- Changing a level updates a source that is already playing.
- Bypass and disabling restore original source gain.
- Minecraft master and category mute remain zero even at 150% group gain.
- Eight overlapping explosions each receive their intended source gain.
- Preview scheduling and cancellation execute without stopping unrelated game audio.
- The replacement resource pack resolves the explosion event to `minecraft:random/orb`; the explosion-group gain still applies correctly.

The plain-resource run recorded 8 grouped checks; the replacement-pack run recorded 9, including its replacement assertion. Some grouped checks include multiple numerical assertions.

## Visual inspection

Actual framebuffer screenshots were captured and inspected for the mixer, preset screen, and Music & Sound integration. The mixer and preset screens were also inspected at a smaller 854x480 window. At the tested GUI scale, controls remained readable and accessible; the mixer paginated its rows at the smaller size. Normal-size capture was 1280x800.

## Boundaries of this validation

- Audio-engine values were verified. No claim of a subjective listening test, comfortable loudness on all hardware, or absence of clipping in an arbitrary combined audio mix is made.
- Windows/JDK 25 was exercised. Java 25 bytecode is produced, but other operating systems were not exercised.
- No live multiplayer fight, long-session soak, or compatibility matrix with third-party sound mods was performed.
- Resource-pack compatibility was tested with one controlled event replacement, not every possible pack.
- This is a per-event gain tool, not a true combined-output compressor/limiter.

The implementation does not change event packets, game actions, hearing radius, or server state. Server acceptance of client modifications remains separate from technical functionality.
