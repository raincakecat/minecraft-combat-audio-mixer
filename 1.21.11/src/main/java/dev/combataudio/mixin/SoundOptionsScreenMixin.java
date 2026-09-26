package dev.combataudio.mixin;

import dev.combataudio.ui.MixerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin extends GameOptionsScreen {
    protected SoundOptionsScreenMixin(Screen parent, GameOptions options, Text title) { super(parent, options, title); }

    @Inject(method = "addOptions", at = @At("HEAD"))
    private void combatAudio$addMixer(CallbackInfo ci) {
        if (body != null) body.addWidgetEntry(ButtonWidget.builder(Text.literal("Combat Audio Mixer..."),
                button -> client.setScreen(new MixerScreen(this))).width(310).build(), (net.minecraft.client.gui.widget.ClickableWidget) null);
    }
}
