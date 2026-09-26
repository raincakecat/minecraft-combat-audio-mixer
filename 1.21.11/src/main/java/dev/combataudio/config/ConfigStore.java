package dev.combataudio.config;

import dev.combataudio.core.MixerSettings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class ConfigStore {
    private final Path directory;
    public ConfigStore(Path directory) { this.directory = directory; }
    public Path directory() { return directory; }
    public Path presetDirectory() { return directory.resolve("presets"); }
    public record Loaded(MixerSettings settings, String warning) {}

    public Loaded load() throws IOException {
        Path config = directory.resolve("config.json");
        if (!Files.exists(config)) return new Loaded(MixerSettings.defaults(), "");
        try {
            PresetCodec.Decoded decoded = read(config);
            return new Loaded(decoded.settings(), String.join(" ", decoded.warnings()));
        } catch (IllegalArgumentException failure) {
            Path recovered = directory.resolve("config.invalid-" + UUID.randomUUID() + ".json");
            Files.move(config, recovered);
            return new Loaded(MixerSettings.defaults(), "Invalid config preserved as " + recovered.getFileName() + ". Defaults loaded.");
        }
    }

    public void save(MixerSettings settings) throws IOException {
        atomicWrite(directory.resolve("config.json"), PresetCodec.encode("Current mix", settings));
    }

    public Path savePreset(String name, MixerSettings settings) throws IOException {
        if (!PresetCodec.validName(name)) throw new IllegalArgumentException("Use 1-40 letters, numbers, spaces, - or _; start with a letter or number.");
        Path path = presetDirectory().resolve(name + ".json");
        atomicWrite(path, PresetCodec.encode(name, settings));
        return path;
    }

    public List<Path> listPresets() throws IOException {
        Files.createDirectories(presetDirectory());
        try (var files = Files.list(presetDirectory())) {
            return files.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .limit(200).toList();
        }
    }

    public PresetCodec.Decoded read(Path path) throws IOException {
        if (Files.size(path) > PresetCodec.MAX_BYTES) throw new IllegalArgumentException("Preset exceeds 64 KiB.");
        return PresetCodec.decode(Files.readString(path, StandardCharsets.UTF_8));
    }

    private static void atomicWrite(Path target, String json) throws IOException {
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), ".mix-", ".tmp");
        try {
            Files.writeString(temporary, json, StandardCharsets.UTF_8);
            if (Files.exists(target)) Files.copy(target, target.resolveSibling(target.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
}
