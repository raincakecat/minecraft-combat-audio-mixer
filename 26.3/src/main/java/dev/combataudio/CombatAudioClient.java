package dev.combataudio;

import dev.combataudio.audio.PreviewPlayer;
import dev.combataudio.config.ConfigStore;
import dev.combataudio.core.GainMath;
import dev.combataudio.core.MixerSettings;
import dev.combataudio.core.SoundGroup;
import dev.combataudio.ui.MixerScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class CombatAudioClient implements ClientModInitializer {
    public static final String MOD_ID = "combat_audio_mixer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ConfigStore STORE = new ConfigStore(FabricLoader.getInstance().getConfigDir().resolve("combat-audio-mixer"));
    public static final PreviewPlayer PREVIEW = new PreviewPlayer();
    private static final Map<Identifier, SoundGroup> ROUTING = createRouting();
    private static volatile MixerSettings settings = MixerSettings.defaults();
    private static volatile boolean bypass;
    private static boolean refreshRequested;
    private static boolean openRequested;
    private static boolean dirty;
    private static int saveCountdown;
    private static String notice = "Changes apply live and save automatically.";

    private static Map<Identifier, SoundGroup> createRouting() {
        Map<Identifier, SoundGroup> result = new HashMap<>();
        for (SoundGroup group : SoundGroup.values()) for (String event : group.events) result.put(Identifier.withDefaultNamespace(event), group);
        return Map.copyOf(result);
    }

    @Override public void onInitializeClient() {
        try {
            ConfigStore.Loaded loaded = STORE.load();
            settings = loaded.settings();
            if (!loaded.warning().isEmpty()) notice = loaded.warning();
        } catch (IOException e) { notice = "Could not read config; using neutral defaults."; LOGGER.warn(notice, e); }

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "controls"));
        // Key code 0 on the KEYBOARD type resolves to InputConstants.UNKNOWN: an unbound key.
        KeyMapping open = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.combat_audio_mixer.open", InputConstants.Type.KEYBOARD, InputConstants.KEY_F8, category));
        KeyMapping compare = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.combat_audio_mixer.bypass", InputConstants.Type.KEYBOARD, 0, category));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(ClientCommands.literal("combataudio").executes(context -> { openRequested = true; return 1; })));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (open.consumeClick()) openRequested = true;
            while (compare.consumeClick()) {
                setBypass(!bypass);
                if (client.player != null) client.player.sendOverlayMessage(Component.literal(bypass ? "Combat Audio: original audio (bypassed)" : "Combat Audio: configured mix"));
            }
            if (openRequested) { openRequested = false; client.setScreenAndShow(new MixerScreen(client.gui.screen())); }
            if (refreshRequested) { refreshRequested = false; client.getSoundManager().refreshCategoryVolume(SoundSource.MASTER); }
            PREVIEW.tick(client);
            if (dirty && --saveCountdown <= 0) saveNow();
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> saveNow());
        if (FabricLoader.getInstance().isDevelopmentEnvironment() && Boolean.getBoolean("combataudio.smokeTest")) {
            dev.combataudio.validation.ClientSmokeTest.register();
        }
        LOGGER.info("Combat Audio Mixer ready: {} supported events, Fabric 26.3", ROUTING.size());
    }

    public static MixerSettings settings() { return settings; }
    public static boolean bypass() { return bypass; }
    public static String notice() { return notice; }
    public static void notice(String value) { notice = value; }
    public static void setSettings(MixerSettings value) {
        settings = value;
        dirty = true;
        saveCountdown = 20;
        refreshRequested = true;
    }
    public static void setBypass(boolean value) { bypass = value; refreshRequested = true; }

    public static float adjust(SoundInstance sound, float vanillaVolume) {
        SoundGroup group = ROUTING.get(sound.getIdentifier());
        if (group == null) return vanillaVolume;
        MixerSettings snapshot = settings;
        return GainMath.apply(vanillaVolume, snapshot.gain(group), snapshot.enabled(), bypass);
    }

    public static void saveNow() {
        if (!dirty) return;
        try { STORE.save(settings); dirty = false; }
        catch (IOException e) { notice = "Save failed; will retry: " + e.getMessage(); saveCountdown = 100; LOGGER.warn("Unable to save mix", e); }
    }
}
