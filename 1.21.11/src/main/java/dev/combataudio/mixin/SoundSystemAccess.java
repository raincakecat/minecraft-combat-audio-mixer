package dev.combataudio.mixin;

import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.Map;

@Mixin(SoundSystem.class)
public interface SoundSystemAccess {
    @Accessor("sources") Map<SoundInstance, Channel.SourceManager> combatAudio$sources();
    @Invoker("getAdjustedVolume") float combatAudio$vanillaVolume(float volume, SoundCategory category);
}
