package dev.combataudio.audio;

import dev.combataudio.core.SoundGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class PreviewPlayer {
    private record Cue(int tick, String event, SoundCategory category, float gain, float pitch) {}
    private final List<Cue> queue = new ArrayList<>();
    private final List<SoundInstance> active = new ArrayList<>();
    private int elapsed;

    public void single(SoundGroup group) {
        stop(MinecraftClient.getInstance());
        String event = switch (group) {
            case EQUIPMENT -> "item.armor.equip_netherite";
            case MELEE -> "entity.player.attack.crit";
            default -> group.events.getFirst();
        };
        queue.add(new Cue(0, event, category(group), .5f, 1f));
    }

    public void sequence() {
        stop(MinecraftClient.getInstance());
        queue.add(new Cue(0, "block.stone.place", SoundCategory.BLOCKS, .4f, 1f));
        queue.add(new Cue(8, "entity.generic.explode", SoundCategory.BLOCKS, .3f, .9f));
        queue.add(new Cue(8, "entity.generic.explode", SoundCategory.BLOCKS, .25f, 1.1f));
        queue.add(new Cue(16, "item.totem.use", SoundCategory.PLAYERS, .4f, 1f));
        queue.add(new Cue(36, "entity.generic.eat", SoundCategory.PLAYERS, .4f, 1f));
        queue.add(new Cue(49, "block.respawn_anchor.charge", SoundCategory.BLOCKS, .4f, 1f));
        queue.add(new Cue(61, "entity.ender_pearl.throw", SoundCategory.NEUTRAL, .4f, 1f));
    }

    public static SoundCategory category(SoundGroup group) {
        return switch (group) {
            case EXPLOSIONS, BLOCKS, ANCHORS -> SoundCategory.BLOCKS;
            case PEARLS -> SoundCategory.NEUTRAL;
            default -> SoundCategory.PLAYERS;
        };
    }

    public void tick(MinecraftClient client) {
        if (!queue.isEmpty()) {
            var iterator = queue.iterator();
            while (iterator.hasNext()) {
                Cue cue = iterator.next();
                if (cue.tick <= elapsed) {
                    SoundInstance sound = new PositionedSoundInstance(Identifier.ofVanilla(cue.event), cue.category,
                            cue.gain, cue.pitch, SoundInstance.createRandom(), false, 0,
                            SoundInstance.AttenuationType.NONE, 0, 0, 0, true);
                    active.add(sound);
                    client.getSoundManager().play(sound);
                    iterator.remove();
                }
            }
            elapsed++;
        }
        active.removeIf(sound -> !client.getSoundManager().isPlaying(sound));
    }

    public void stop(MinecraftClient client) {
        queue.clear();
        elapsed = 0;
        for (SoundInstance sound : active) client.getSoundManager().stop(sound);
        active.clear();
    }
}
