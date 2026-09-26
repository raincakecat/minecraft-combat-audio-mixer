package dev.combataudio.audio;

import dev.combataudio.core.SoundGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;

public final class PreviewPlayer {
    private record Cue(int tick, String event, SoundSource source, float gain, float pitch) {}
    private final List<Cue> queue = new ArrayList<>();
    private final List<SoundInstance> active = new ArrayList<>();
    private int elapsed;

    public void single(SoundGroup group) {
        stop(Minecraft.getInstance());
        String event = switch (group) {
            case EQUIPMENT -> "item.armor.equip_netherite";
            case MELEE -> "entity.player.attack.crit";
            default -> group.events.getFirst();
        };
        queue.add(new Cue(0, event, category(group), .5f, 1f));
    }

    public void sequence() {
        stop(Minecraft.getInstance());
        queue.add(new Cue(0, "block.stone.place", SoundSource.BLOCKS, .4f, 1f));
        queue.add(new Cue(8, "entity.generic.explode", SoundSource.BLOCKS, .3f, .9f));
        queue.add(new Cue(8, "entity.generic.explode", SoundSource.BLOCKS, .25f, 1.1f));
        queue.add(new Cue(16, "item.totem.use", SoundSource.PLAYERS, .4f, 1f));
        queue.add(new Cue(36, "entity.generic.eat", SoundSource.PLAYERS, .4f, 1f));
        queue.add(new Cue(49, "block.respawn_anchor.charge", SoundSource.BLOCKS, .4f, 1f));
        queue.add(new Cue(61, "entity.ender_pearl.throw", SoundSource.NEUTRAL, .4f, 1f));
    }

    public static SoundSource category(SoundGroup group) {
        return switch (group) {
            case EXPLOSIONS, BLOCKS, ANCHORS -> SoundSource.BLOCKS;
            case PEARLS -> SoundSource.NEUTRAL;
            default -> SoundSource.PLAYERS;
        };
    }

    public void tick(Minecraft client) {
        if (!queue.isEmpty()) {
            var iterator = queue.iterator();
            while (iterator.hasNext()) {
                Cue cue = iterator.next();
                if (cue.tick <= elapsed) {
                    // Official 26.3 argument order: (…, RandomSource, boolean looping, int delay,
                    // Attenuation, double x, double y, double z, boolean relative).
                    SoundInstance sound = new SimpleSoundInstance(Identifier.withDefaultNamespace(cue.event), cue.source(),
                            cue.gain, cue.pitch, SoundInstance.createUnseededRandom(), true, 0,
                            SoundInstance.Attenuation.NONE, 0, 0, 0, false);
                    active.add(sound);
                    client.getSoundManager().play(sound);
                    iterator.remove();
                }
            }
            elapsed++;
        }
        active.removeIf(sound -> !client.getSoundManager().isActive(sound));
    }

    public void stop(Minecraft client) {
        queue.clear();
        elapsed = 0;
        for (SoundInstance sound : active) client.getSoundManager().stop(sound);
        active.clear();
    }
}
