package dev.combataudio;

import dev.combataudio.config.*;
import dev.combataudio.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class ConfigStoreTest {
    @TempDir Path directory;
    @Test void firstRunUsesNeutral() throws Exception { assertEquals(MixerSettings.defaults(), new ConfigStore(directory).load().settings()); }
    @Test void saveReloadAndPreviousBackup() throws Exception {
        var store = new ConfigStore(directory);
        store.save(BuiltInPresets.FOCUS);
        store.save(BuiltInPresets.QUIET);
        assertEquals(BuiltInPresets.QUIET, store.load().settings());
        assertEquals(BuiltInPresets.FOCUS, store.read(directory.resolve("config.json.bak")).settings());
        try (var files = Files.list(directory)) { assertFalse(files.anyMatch(p -> p.toString().endsWith(".tmp"))); }
    }
    @Test void invalidConfigPreserved() throws Exception {
        Files.writeString(directory.resolve("config.json"), "invalid json");
        var loaded = new ConfigStore(directory).load();
        assertEquals(MixerSettings.defaults(), loaded.settings());
        assertFalse(loaded.warning().isBlank());
        try (var files = Files.list(directory)) { assertTrue(files.anyMatch(p -> p.getFileName().toString().startsWith("config.invalid-"))); }
    }
    @Test void presetsArePortableAndListedWithoutBackups() throws Exception {
        var store = new ConfigStore(directory);
        var path = store.savePreset("Focus example", BuiltInPresets.FOCUS);
        store.savePreset("Focus example", BuiltInPresets.QUIET);
        assertEquals(1, store.listPresets().size());
        assertEquals(BuiltInPresets.QUIET, store.read(path).settings());
        assertEquals(BuiltInPresets.FOCUS, store.read(path.resolveSibling("Focus example.json.bak")).settings());
    }
    @Test void badNameCannotEscapeDirectory() {
        assertThrows(IllegalArgumentException.class, () -> new ConfigStore(directory).savePreset("../outside", MixerSettings.defaults()));
    }
    @Test void oversizedFileRejected() throws Exception {
        Path file = directory.resolve("big.json");
        Files.writeString(file, " ".repeat(PresetCodec.MAX_BYTES + 1));
        assertThrows(IllegalArgumentException.class, () -> new ConfigStore(directory).read(file));
    }
}
