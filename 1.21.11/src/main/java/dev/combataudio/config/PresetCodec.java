package dev.combataudio.config;

import com.google.gson.*;
import dev.combataudio.core.MixerSettings;
import dev.combataudio.core.SoundGroup;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class PresetCodec {
    public static final String MAPPING_VERSION = "1.21.11-v1";
    public static final int MAX_BYTES = 65_536;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record Decoded(String name, MixerSettings settings, List<String> warnings) {}

    public static String encode(String name, MixerSettings settings) {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", 1);
        root.addProperty("mappingVersion", MAPPING_VERSION);
        root.addProperty("name", name);
        root.addProperty("enabled", settings.enabled());
        JsonObject gains = new JsonObject();
        for (SoundGroup group : SoundGroup.values()) gains.addProperty(group.key, settings.gain(group));
        root.add("gains", gains);
        return GSON.toJson(root) + "\n";
    }

    public static Decoded decode(String input) {
        if (input.length() > MAX_BYTES || input.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES)
            throw new IllegalArgumentException("Preset exceeds 64 KiB.");
        try {
            JsonObject root = JsonParser.parseString(input).getAsJsonObject();
            JsonElement schema = root.get("schemaVersion");
            if (schema == null || !schema.isJsonPrimitive() || !schema.getAsJsonPrimitive().isNumber()
                    || schema.getAsDouble() != 1.0) throw new IllegalArgumentException("Unsupported preset schema; expected version 1.");
            String name = root.has("name") ? root.get("name").getAsString() : "Imported";
            if (!validName(name)) name = "Imported";
            List<String> warnings = new ArrayList<>();
            if (!root.has("mappingVersion") || !MAPPING_VERSION.equals(root.get("mappingVersion").getAsString()))
                warnings.add("Different sound mapping; recognized groups were imported.");
            boolean enabled = true;
            if (root.has("enabled")) {
                JsonElement e = root.get("enabled");
                if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isBoolean()) throw new IllegalArgumentException("enabled must be true or false.");
                enabled = e.getAsBoolean();
            }
            JsonObject values = root.getAsJsonObject("gains");
            if (values == null) throw new IllegalArgumentException("Preset must contain a gains object.");
            Map<SoundGroup, Float> gains = new EnumMap<>(SoundGroup.class);
            for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
                SoundGroup group = SoundGroup.fromKey(entry.getKey());
                if (group == null) { warnings.add("Skipped unknown group: " + entry.getKey()); continue; }
                JsonElement value = entry.getValue();
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException("Gain must be numeric: " + entry.getKey());
                double gain = value.getAsDouble();
                if (!Double.isFinite(gain) || gain < 0 || gain > 1.5) throw new IllegalArgumentException("Gain must be from 0 to 1.5: " + entry.getKey());
                gains.put(group, (float) gain);
            }
            return new Decoded(name, new MixerSettings(enabled, gains), List.copyOf(warnings));
        } catch (JsonParseException | IllegalStateException | ClassCastException | UnsupportedOperationException e) {
            throw new IllegalArgumentException("Invalid preset JSON: " + e.getClass().getSimpleName(), e);
        }
    }

    public static boolean validName(String name) {
        return name != null && name.matches("[A-Za-z0-9][A-Za-z0-9 _-]{0,39}") && name.equals(name.trim());
    }
}
