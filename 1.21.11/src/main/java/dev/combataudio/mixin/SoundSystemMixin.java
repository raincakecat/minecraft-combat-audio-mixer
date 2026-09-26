package dev.combataudio.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.combataudio.CombatAudioClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {
    // Modify only source gain, AFTER the unmodified source volume determines radius.
    @ModifyExpressionValue(
            method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundSystem;getAdjustedVolume(FLnet/minecraft/sound/SoundCategory;)F"))
    private float combatAudio$initialGain(float vanillaVolume, SoundInstance sound) {
        return CombatAudioClient.adjust(sound, vanillaVolume);
    }

    // Tickable sounds, category changes and live preset/bypass refreshes take this path.
    @Inject(method = "getAdjustedVolume(Lnet/minecraft/client/sound/SoundInstance;)F", at = @At("RETURN"), cancellable = true)
    private void combatAudio$refreshedGain(SoundInstance sound, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(CombatAudioClient.adjust(sound, cir.getReturnValue()));
    }
}
