package dev.combataudio.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.combataudio.CombatAudioClient;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public abstract class SoundSystemMixin {
    // Modify only source gain, AFTER the unmodified source volume determines radius.
    @ModifyExpressionValue(
            method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(FLnet/minecraft/sounds/SoundSource;)F"))
    private float combatAudio$initialGain(float vanillaVolume, SoundInstance sound) {
        return CombatAudioClient.adjust(sound, vanillaVolume);
    }

    // Tickable sounds, category changes and live preset/bypass refreshes take this path.
    @Inject(method = "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F", at = @At("RETURN"), cancellable = true)
    private void combatAudio$refreshedGain(SoundInstance sound, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(CombatAudioClient.adjust(sound, cir.getReturnValue()));
    }
}
