package dev.combataudio.mixin;

import net.minecraft.client.sound.Source;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Source.class)
public interface SourceAccess {
    @Accessor("pointer") int combatAudio$pointer();
}
