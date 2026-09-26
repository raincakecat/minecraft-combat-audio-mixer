package dev.combataudio.ui;

import dev.combataudio.CombatAudioClient;
import dev.combataudio.core.MixerSettings;
import dev.combataudio.core.SoundGroup;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MixerScreen extends Screen {
    private final Screen parent;
    private int page;
    private int rows;
    private int left;
    private int panelWidth;

    public MixerScreen(Screen parent) { super(Component.literal("Combat Audio Mixer")); this.parent = parent; }

    @Override protected void init() {
        panelWidth = Math.min(520, width - 24);
        left = (width - panelWidth) / 2;
        rows = Math.max(1, Math.min(8, (height - 145) / 28));
        int pages = (SoundGroup.values().length + rows - 1) / rows;
        page = Math.min(page, pages - 1);
        int third = (panelWidth - 12) / 3;
        button(CombatAudioClient.settings().enabled() ? "Mixer: ON" : "Mixer: OFF", left, 43, third, () -> {
            CombatAudioClient.setSettings(CombatAudioClient.settings().withEnabled(!CombatAudioClient.settings().enabled())); rebuildWidgets();
        });
        button(CombatAudioClient.bypass() ? "Original audio" : "Configured mix", left + third + 6, 43, third, () -> {
            CombatAudioClient.setBypass(!CombatAudioClient.bypass()); rebuildWidgets();
        }).setTooltip(Tooltip.create(Component.literal("Compare with original audio. This temporary bypass is not saved.")));
        button("Presets...", left + (third + 6) * 2, 43, panelWidth - (third + 6) * 2, () -> minecraft.setScreenAndShow(new PresetScreen(this)));

        for (int row = 0; row < rows; row++) {
            int index = page * rows + row;
            if (index >= SoundGroup.values().length) break;
            SoundGroup group = SoundGroup.values()[index];
            int y = 76 + row * 28;
            AbstractSliderButton slider = new AbstractSliderButton(left, y, panelWidth - 66, 20, label(group), CombatAudioClient.settings().gain(group) / 1.5) {
                @Override protected void updateMessage() { setMessage(Component.literal(group.label + ": " + Math.round(value * 150) + "%")); }
                @Override protected void applyValue() {
                    float gain = Math.round(value * 150) / 100f;
                    CombatAudioClient.setSettings(CombatAudioClient.settings().withGain(group, gain));
                }
            };
            String note = group == SoundGroup.EXPLOSIONS ? "Shared crystal/anchor/TNT explosion event.\n" : group == SoundGroup.BLOCKS ? "Also affects other stone/glass placement.\n" : "";
            slider.setTooltip(Tooltip.create(Component.literal(note + "100% = unchanged; 0% = muted.\n" + String.join("\n", group.events))));
            addRenderableWidget(slider);
            button("Preview", left + panelWidth - 60, y, 60, () -> CombatAudioClient.PREVIEW.single(group))
                    .setTooltip(Tooltip.create(Component.literal("Uses vanilla category volume and a modest preview level.")));
        }

        Button previous = button("<", left, height - 61, 24, () -> { page--; rebuildWidgets(); });
        previous.active = page > 0;
        Button next = button(">", left + 30, height - 61, 24, () -> { page++; rebuildWidgets(); });
        next.active = page < pages - 1;
        button("Preview mix", left + panelWidth - 178, height - 61, 112, () -> CombatAudioClient.PREVIEW.sequence());
        button("Stop", left + panelWidth - 60, height - 61, 60, () -> CombatAudioClient.PREVIEW.stop(minecraft));
        button("Reset levels", left, height - 35, panelWidth / 2 - 3, () -> {
            CombatAudioClient.setSettings(MixerSettings.defaults().withEnabled(CombatAudioClient.settings().enabled()));
            CombatAudioClient.notice("All group levels reset to 100%."); rebuildWidgets();
        });
        button("Done", left + panelWidth / 2 + 3, height - 35, panelWidth / 2 - 3, this::onClose);
    }

    private Component label(SoundGroup group) { return Component.literal(group.label + ": " + Math.round(CombatAudioClient.settings().gain(group) * 100) + "%"); }
    private Button button(String label, int x, int y, int w, Runnable action) {
        return addRenderableWidget(Button.builder(Component.literal(label), b -> action.run()).bounds(x, y, w, 20).build());
    }

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fillGradient(0, 0, width, height, 0xFF0D1426, 0xFF192438);
        graphics.fill(left - 6, 70, left + panelWidth + 6, height - 69, 0x80242F48);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.centeredText(font, title, width / 2, 12, 0xFFE4DFFF);
        graphics.centeredText(font, Component.literal("Your sounds. Your balance.  |  Fabric 26.3"), width / 2, 27, 0xFF9EAAC3);
        graphics.text(font, "Page " + (page + 1) + "/" + ((8 + rows - 1) / rows), left + 61, height - 55, 0xFFBAC3D6);
        String status = CombatAudioClient.bypass() ? "Bypassed: hearing original audio. Click Original audio to return." : CombatAudioClient.notice();
        graphics.centeredText(font, Component.literal(font.plainSubstrByWidth(status, width - 16)), width / 2, height - 11, 0xFFB9C3D8);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { CombatAudioClient.saveNow(); minecraft.setScreenAndShow(parent); }
    @Override public void removed() { CombatAudioClient.PREVIEW.stop(minecraft); CombatAudioClient.saveNow(); }
}
