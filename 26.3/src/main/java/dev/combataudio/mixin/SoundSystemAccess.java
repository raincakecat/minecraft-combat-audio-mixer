package dev.combataudio.mixin;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.Map;

@Mixin(SoundEngine.class)
public interface SoundSystemAccess {
    @Accessor("instanceToChannel") Map<SoundInstance, ChannelAccess.ChannelHandle> combatAudio$sources();
    @Invoker("calculateVolume") float combatAudio$vanillaVolume(float volume, SoundSource source);
}
