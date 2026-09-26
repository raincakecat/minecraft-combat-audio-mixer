package dev.combataudio.validation;

import com.mojang.blaze3d.audio.Channel;
import dev.combataudio.CombatAudioClient;
import dev.combataudio.core.*;
import dev.combataudio.mixin.*;
import dev.combataudio.ui.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.openal.AL10;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Opt-in, isolated dev-client checks. Never registered in a production instance. */
public final class ClientSmokeTest {
    private static final List<String> checks = new ArrayList<>();
    private static int ticks;
    private static int stage;
    private static int stageTick;
    private ClientSmokeTest() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                ticks++;
                if (ticks > 2400) throw new IllegalStateException("Smoke test timed out");
                if (client.gui.overlay() != null || client.gui.screen() == null) return;
                if (stage == 0 && ticks > 30) {
                    client.options.pauseOnLostFocus = false;
                    client.options.guiScale().set(2);
                    client.resizeGui();
                    runAudioChecks(client);
                    CombatAudioClient.setSettings(BuiltInPresets.FOCUS);
                    client.setScreenAndShow(new MixerScreen(null));
                    stage = 1; stageTick = ticks;
                } else if (stage == 1 && ticks - stageTick > 15) {
                    screenshot(client, "mixer.png");
                    client.setScreenAndShow(new PresetScreen(new MixerScreen(null)));
                    stage = 2; stageTick = ticks;
                } else if (stage == 2 && ticks - stageTick > 15) {
                    screenshot(client, "presets.png");
                    client.setScreenAndShow(new SoundOptionsScreen(null, client.options));
                    stage = 3; stageTick = ticks;
                } else if (stage == 3 && ticks - stageTick > 15) {
                    screenshot(client, "sound-options.png");
                    checks.add("Mixer and preset screens rendered; Sound Options integration initialized");
                    client.getWindow().setWindowed(854, 480);
                    client.setScreenAndShow(new MixerScreen(null));
                    stage = 4; stageTick = ticks;
                } else if (stage == 4 && ticks - stageTick > 15) {
                    screenshot(client, "mixer-small.png");
                    client.setScreenAndShow(new PresetScreen(null));
                    stage = 5; stageTick = ticks;
                } else if (stage == 5 && ticks - stageTick > 15) {
                    screenshot(client, "presets-small.png");
                    checks.add("Small-window layouts rendered");
                    CombatAudioClient.setSettings(MixerSettings.defaults());
                    CombatAudioClient.setBypass(false);
                    CombatAudioClient.saveNow();
                    stage = 6; stageTick = ticks;
                } else if (stage == 6 && ticks - stageTick > 20) {
                    Files.writeString(gameDir().resolve("smoke-results.txt"), "PASS\n" + String.join("\n", checks) + "\n");
                    CombatAudioClient.LOGGER.info("COMBAT_AUDIO_SMOKE_PASS: {} checks", checks.size());
                    stage = 7;
                    client.stop();
                }
            } catch (Throwable failure) {
                try { Files.writeString(gameDir().resolve("smoke-results.txt"), "FAIL\n" + failure + "\n" + String.join("\n", checks)); } catch (Exception ignored) { }
                CombatAudioClient.LOGGER.error("COMBAT_AUDIO_SMOKE_FAIL", failure);
                stage = 7;
                client.stop();
            }
        });
    }

    private static Path gameDir() { return FabricLoader.getInstance().getGameDir().toAbsolutePath(); }

    private static void runAudioChecks(Minecraft client) throws Exception {
        for (SoundGroup group : SoundGroup.values()) for (String event : group.events)
            require(BuiltInRegistries.SOUND_EVENT.containsKey(Identifier.withDefaultNamespace(event)), "Missing mapped event: " + event);
        checks.add("All supported event IDs exist in Minecraft 26.3");
        client.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(.2);
        client.options.getSoundSourceOptionInstance(SoundSource.BLOCKS).set(.5);
        SoundSystemAccess system = (SoundSystemAccess) ((SoundManagerAccess) client.getSoundManager()).combatAudio$system();
        CombatAudioClient.setSettings(MixerSettings.defaults().withGain(SoundGroup.EXPLOSIONS, .35f));
        CombatAudioClient.setBypass(false);
        SimpleSoundInstance sound = new SimpleSoundInstance(Identifier.withDefaultNamespace("entity.generic.explode"), SoundSource.BLOCKS,
                .1f, .9f, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.LINEAR, 2, 1, -3, true);
        client.getSoundManager().play(sound);
        if (Boolean.getBoolean("combataudio.smokePack")) {
            require(sound.getSound().getLocation().equals(Identifier.withDefaultNamespace("random/orb")), "Test resource pack did not replace explosion audio");
            checks.add("Resource-pack replacement resolved while retaining explosion-group gain");
        }
        ChannelAccess.ChannelHandle manager = system.combatAudio$sources().get(sound);
        require(manager != null, "OpenAL source was not created; no working sound device?");
        float baseline = system.combatAudio$vanillaVolume(sound.getVolume(), sound.getSource());
        near(sourceFloat(manager, AL10.AL_GAIN), baseline * .35f, "Initial source gain");
        near(sourceFloat(manager, AL10.AL_MAX_DISTANCE), Math.max(sound.getVolume(), 1f) * sound.getSound().getAttenuationDistance(), "Original attenuation radius");
        near(sourceFloat(manager, AL10.AL_PITCH), .9f, "Original pitch");
        float[] position = sourcePosition(manager);
        near(position[0], 2, "Source X"); near(position[1], 1, "Source Y"); near(position[2], -3, "Source Z");
        checks.add("Initial OpenAL gain scaled; pitch, position and attenuation radius preserved");

        CombatAudioClient.setSettings(CombatAudioClient.settings().withGain(SoundGroup.EXPLOSIONS, .6f));
        client.getSoundManager().refreshCategoryVolume(SoundSource.MASTER);
        near(sourceFloat(manager, AL10.AL_GAIN), baseline * .6f, "Live gain update");
        CombatAudioClient.setBypass(true);
        client.getSoundManager().refreshCategoryVolume(SoundSource.MASTER);
        near(sourceFloat(manager, AL10.AL_GAIN), baseline, "Bypass restores original");
        CombatAudioClient.setBypass(false);
        CombatAudioClient.setSettings(CombatAudioClient.settings().withEnabled(false));
        client.getSoundManager().refreshCategoryVolume(SoundSource.MASTER);
        near(sourceFloat(manager, AL10.AL_GAIN), baseline, "Disabled restores original");
        checks.add("Live level changes, bypass and disabling update already-playing audio");

        CombatAudioClient.setSettings(MixerSettings.defaults().withGain(SoundGroup.EXPLOSIONS, 1.5f));
        client.options.getSoundSourceOptionInstance(SoundSource.BLOCKS).set(0.0);
        client.getSoundManager().refreshCategoryVolume(SoundSource.MASTER);
        near(sourceFloat(manager, AL10.AL_GAIN), 0, "Muted category stays muted");
        client.options.getSoundSourceOptionInstance(SoundSource.BLOCKS).set(.5);
        client.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(0.0);
        client.getSoundManager().refreshCategoryVolume(SoundSource.MASTER);
        near(sourceFloat(manager, AL10.AL_GAIN), 0, "Muted master stays muted");
        checks.add("Master and category muting remain effective even at 150% group gain");
        client.getSoundManager().stop(sound);
        client.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(.2);
        client.options.getSoundSourceOptionInstance(SoundSource.BLOCKS).set(1.0);

        CombatAudioClient.setSettings(BuiltInPresets.FOCUS);
        List<SoundInstance> overlapping = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            SoundInstance burst = new SimpleSoundInstance(Identifier.withDefaultNamespace("entity.generic.explode"), SoundSource.BLOCKS,
                    .02f, 1f, SoundInstance.createUnseededRandom(), true, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, false);
            client.getSoundManager().play(burst);
            ChannelAccess.ChannelHandle burstManager = system.combatAudio$sources().get(burst);
            require(burstManager != null, "Missing overlapping source");
            near(sourceFloat(burstManager, AL10.AL_GAIN), system.combatAudio$vanillaVolume(burst.getVolume(), burst.getSource()) * .35f, "Overlapping gain");
            overlapping.add(burst);
        }
        for (SoundInstance burst : overlapping) client.getSoundManager().stop(burst);
        checks.add("Eight overlapping explosions each use configured source gain");
        CombatAudioClient.PREVIEW.sequence();
        CombatAudioClient.PREVIEW.stop(client);
        checks.add("Preview sequence queues and cancels independently of game sounds");
    }

    private static float sourceFloat(ChannelAccess.ChannelHandle manager, int property) throws Exception {
        CompletableFuture<Float> value = new CompletableFuture<>();
        manager.execute(source -> { try { value.complete(AL10.alGetSourcef(((SourceAccess) source).combatAudio$pointer(), property)); } catch (Throwable t) { value.completeExceptionally(t); } });
        return value.get(5, TimeUnit.SECONDS);
    }
    private static float[] sourcePosition(ChannelAccess.ChannelHandle manager) throws Exception {
        CompletableFuture<float[]> value = new CompletableFuture<>();
        manager.execute(source -> { float[] position = new float[3]; AL10.alGetSourcefv(((SourceAccess) source).combatAudio$pointer(), AL10.AL_POSITION, position); value.complete(position); });
        return value.get(5, TimeUnit.SECONDS);
    }
    private static void near(float actual, float expected, String label) { require(Math.abs(actual - expected) < .0001f, label + ": expected " + expected + ", got " + actual); }
    private static void require(boolean pass, String message) { if (!pass) throw new IllegalStateException(message); }
    private static void screenshot(Minecraft client, String name) {
        Screenshot.grab(FabricLoader.getInstance().getGameDir().toFile(), name, client.gameRenderer.mainRenderTarget(), 1, text -> CombatAudioClient.LOGGER.info("Smoke screenshot: {}", text.getString()));
    }
}
