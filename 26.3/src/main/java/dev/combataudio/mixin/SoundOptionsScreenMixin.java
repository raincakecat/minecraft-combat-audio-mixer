package dev.combataudio.mixin;

import dev.combataudio.ui.MixerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin extends OptionsSubScreen {
    protected SoundOptionsScreenMixin(Screen parent, Options options, Component title) { super(parent, options, title); }

    @Inject(method = "addOptions", at = @At("HEAD"))
    private void combatAudio$addMixer(CallbackInfo ci) {
        if (list != null) list.addBig(Button.builder(Component.literal("Combat Audio Mixer..."),
                button -> minecraft.setScreenAndShow(new MixerScreen(this))).width(310).build());
    }
}
