package dev.combataudio.ui;

import com.mojang.blaze3d.Blaze3D;
import dev.combataudio.CombatAudioClient;
import dev.combataudio.config.PresetCodec;
import dev.combataudio.core.BuiltInPresets;
import dev.combataudio.core.MixerSettings;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PresetScreen extends Screen {
    private record Choice(String name, MixerSettings builtIn, Path path) {}
    private final Screen parent;
    private final List<Choice> choices = new ArrayList<>();
    private EditBox nameField;
    private String draftName = "My mix";
    private String message = "Drop one preset JSON file here, or import from clipboard.";
    private int selected;
    private int left;
    private int panelWidth;

    public PresetScreen(Screen parent) { super(Component.literal("Audio presets")); this.parent = parent; }

    @Override protected void init() {
        panelWidth = Math.min(460, width - 24);
        left = (width - panelWidth) / 2;
        choices.clear();
        choices.add(new Choice("Neutral (100%)", BuiltInPresets.NEUTRAL, null));
        choices.add(new Choice("Focus", BuiltInPresets.FOCUS, null));
        choices.add(new Choice("Quiet", BuiltInPresets.QUIET, null));
        try { for (Path path : CombatAudioClient.STORE.listPresets()) choices.add(new Choice(path.getFileName().toString().replaceFirst("\\.json$", ""), null, path)); }
        catch (Exception e) { message = "Could not list presets: " + e.getMessage(); }
        selected = Math.min(selected, choices.size() - 1);
        int half = (panelWidth - 6) / 2;
        button("<", left, 49, 24, () -> { preserveName(); selected = Math.floorMod(selected - 1, choices.size()); rebuildWidgets(); });
        button(choices.get(selected).name, left + 30, 49, panelWidth - 90, () -> { preserveName(); selected = (selected + 1) % choices.size(); rebuildWidgets(); })
                .setTooltip(Tooltip.create(Component.literal("Click to cycle through built-in and saved presets.")));
        button("Apply", left + panelWidth - 54, 49, 54, this::applySelected);

        nameField = new EditBox(font, left, 88, panelWidth, 20, Component.literal("Preset name"));
        nameField.setMaxLength(40);
        nameField.setValue(draftName);
        addRenderableWidget(nameField);
        button("Save / export file", left, 116, half, () -> {
            try {
                Path saved = CombatAudioClient.STORE.savePreset(nameField.getValue(), CombatAudioClient.settings());
                message = "Saved " + saved.getFileName() + " (previous file kept as .bak).";
                preserveName(); rebuildWidgets();
            } catch (Exception e) { message = e.getMessage(); }
        }).setTooltip(Tooltip.create(Component.literal("Saves a portable JSON in the presets folder. Same name replaces that preset with a backup.")));
        button("Open presets folder", left + half + 6, 116, half, () -> {
            try { CombatAudioClient.STORE.listPresets(); Blaze3D.openPath(CombatAudioClient.STORE.presetDirectory()); }
            catch (Exception e) { message = "Could not open folder: " + e.getMessage(); }
        });
        button("Copy preset JSON", left, 142, half, () -> {
            minecraft.keyboardHandler.setClipboard(PresetCodec.encode(PresetCodec.validName(nameField.getValue()) ? nameField.getValue() : "My mix", CombatAudioClient.settings()));
            message = "Preset JSON copied. Paste it into a file or message.";
        });
        button("Import clipboard", left + half + 6, 142, half, () -> {
            try { importDecoded(PresetCodec.decode(minecraft.keyboardHandler.getClipboard())); }
            catch (Exception e) { message = "Import failed: " + e.getMessage(); }
        });
        button("Back to mixer", left, height - 33, panelWidth, this::onClose);
    }

    private void preserveName() { if (nameField != null) draftName = nameField.getValue(); }
    private void applySelected() {
        Choice choice = choices.get(selected);
        try {
            if (choice.builtIn != null) importDecoded(new PresetCodec.Decoded(choice.name.replace(" (100%)", ""), choice.builtIn, List.of()));
            else importDecoded(CombatAudioClient.STORE.read(choice.path));
        } catch (Exception e) { message = "Could not load preset: " + e.getMessage(); }
    }
    private void importDecoded(PresetCodec.Decoded decoded) {
        CombatAudioClient.setSettings(decoded.settings());
        CombatAudioClient.setBypass(false);
        draftName = decoded.name();
        if (nameField != null) nameField.setValue(draftName);
        message = decoded.warnings().isEmpty() ? "Applied " + decoded.name() + ". Save / export to keep a named copy." : String.join(" ", decoded.warnings());
        CombatAudioClient.notice("Applied preset: " + decoded.name());
    }
    @Override public void onFilesDrop(List<Path> paths) {
        if (paths.size() != 1 || !paths.getFirst().getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".json")) {
            message = "Drop exactly one JSON preset (up to 64 KiB)."; return;
        }
        try { importDecoded(CombatAudioClient.STORE.read(paths.getFirst())); }
        catch (Exception e) { message = "Import failed: " + e.getMessage(); }
    }
    private Button button(String label, int x, int y, int w, Runnable action) {
        return addRenderableWidget(Button.builder(Component.literal(label), b -> action.run()).bounds(x, y, w, 20).build());
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) { graphics.fillGradient(0, 0, width, height, 0xFF0D1426, 0xFF192438); }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.centeredText(font, title, width / 2, 13, 0xFFE4DFFF);
        graphics.centeredText(font, Component.literal("Local presets. No account or upload required."), width / 2, 29, 0xFF9EAAC3);
        graphics.text(font, "Name for saving", left, 76, 0xFFBAC3D6);
        int y = 178;
        for (var line : font.split(Component.literal(message), panelWidth)) {
            if (y + 9 >= height - 39) break;
            graphics.text(font, line, left, y, 0xFFC6CEDF); y += 11;
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { CombatAudioClient.saveNow(); minecraft.setScreenAndShow(parent); }
    @Override public void removed() { preserveName(); CombatAudioClient.saveNow(); }
}
