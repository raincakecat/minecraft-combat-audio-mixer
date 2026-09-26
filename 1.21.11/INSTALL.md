# Install Combat Audio Mixer

For **Minecraft Java 1.21.11 with Fabric**.

1. Extract the installation ZIP.
2. Open the `mods` folder inside the extracted ZIP.
3. Copy `combat-audio-mixer-1.0.0+mc1.21.11.jar` into the `mods` folder of your **1.21.11 Fabric instance**.
4. Make sure that instance also has Fabric API for 1.21.11. The included `fabric-api-0.141.6+1.21.11.jar` is the version tested with this release. Keep only one compatible Fabric API JAR; do not add a second copy if it is already present.
5. Launch the instance using Fabric Loader 0.18.4 or newer.

Use your launcher's instance folder if it has a custom game directory. Do not copy the ZIP itself into `mods`. This mod is for the client, not a Minecraft server.

## Open the controls

- **Options -> Music & Sound -> Combat Audio Mixer...**
- **F8** while in a world.
- **/combataudio**, a client-side command.

Change the F8 binding in Controls -> Key Binds -> Combat Audio Mixer if another mod uses it.

## Start with a preset

Open Presets, click the displayed preset name to cycle to Focus or Quiet, and press Apply. Neutral leaves every group at 100%. Then adjust any individual sliders and use Preview to hear samples.

- Configured mix / Original audio compares the result with a temporary bypass.
- Mixer ON/OFF is saved across restarts.
- Preview mix includes overlapping explosions.
- Reset levels returns sliders to 100%.

## Save or share

Enter a name on the Presets screen and click Save / export file. Open presets folder reveals the resulting JSON. You can also copy JSON to the clipboard, import it, or drag a preset JSON onto that screen.

Examples included in the bundle can be dropped onto the Presets screen. Current settings save automatically under `config/combat-audio-mixer` in your selected instance.

## If a preview is silent

Check the group's level, Minecraft's Master Volume, and the relevant vanilla category. The mod respects those settings. Explosion and anchor previews use Blocks; totem/food/equipment/melee use Players; pearl throws use Friendly Mobs.

## Coverage

There are eight groups covering 27 named game sound events. Crystals, anchors, TNT, and other sources using Minecraft's generic explosion event share one explosion slider. This release does not separate their sources or add a combined-output loudness limiter.

See README.md, SUPPORTED-SOUNDS.md, and VALIDATION.md for the full details.
