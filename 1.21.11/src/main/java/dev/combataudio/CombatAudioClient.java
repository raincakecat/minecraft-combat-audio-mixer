package dev.combataudio;

import dev.combataudio.audio.PreviewPlayer;
import dev.combataudio.config.ConfigStore;
import dev.combataudio.core.GainMath;
import dev.combataudio.core.MixerSettings;
import dev.combataudio.core.SoundGroup;
import dev.combataudio.ui.MixerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

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
        for (SoundGroup group : SoundGroup.values()) for (String event : group.events) result.put(Identifier.ofVanilla(event), group);
        return Map.copyOf(result);
    }

    @Override public void onInitializeClient() {
        try {
            ConfigStore.Loaded loaded = STORE.load();
            settings = loaded.settings();
            if (!loaded.warning().isEmpty()) notice = loaded.warning();
        } catch (IOException e) { notice = "Could not read config; using neutral defaults."; LOGGER.warn(notice, e); }

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "controls"));
        KeyBinding open = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.combat_audio_mixer.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F8, category));
        KeyBinding compare = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.combat_audio_mixer.bypass", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(literal("combataudio").executes(context -> { openRequested = true; return 1; })));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (open.wasPressed()) openRequested = true;
            while (compare.wasPressed()) {
                setBypass(!bypass);
                if (client.player != null) client.player.sendMessage(Text.literal(bypass ? "Combat Audio: original audio (bypassed)" : "Combat Audio: configured mix"), true);
            }
            if (openRequested) { openRequested = false; client.setScreen(new MixerScreen(client.currentScreen)); }
            if (refreshRequested) { refreshRequested = false; client.getSoundManager().refreshSoundVolumes(SoundCategory.MASTER); }
            PREVIEW.tick(client);
            if (dirty && --saveCountdown <= 0) saveNow();
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> saveNow());
        if (FabricLoader.getInstance().isDevelopmentEnvironment() && Boolean.getBoolean("combataudio.smokeTest")) {
            dev.combataudio.validation.ClientSmokeTest.register();
        }
        LOGGER.info("Combat Audio Mixer ready: {} supported events, Fabric 1.21.11", ROUTING.size());
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
        SoundGroup group = ROUTING.get(sound.getId());
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
