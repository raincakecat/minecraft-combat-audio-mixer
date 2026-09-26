package dev.combataudio.core;

import java.util.Map;

public final class BuiltInPresets {
    private BuiltInPresets() {}
    public static final MixerSettings NEUTRAL = MixerSettings.defaults();
    public static final MixerSettings FOCUS = new MixerSettings(true, Map.of(
            SoundGroup.EXPLOSIONS, .35f, SoundGroup.TOTEMS, 1f, SoundGroup.EATING, .85f,
            SoundGroup.BLOCKS, .55f, SoundGroup.ANCHORS, .8f, SoundGroup.PEARLS, 1f,
            SoundGroup.MELEE, .8f, SoundGroup.EQUIPMENT, .65f));
    public static final MixerSettings QUIET = new MixerSettings(true, Map.of(
            SoundGroup.EXPLOSIONS, .15f, SoundGroup.TOTEMS, .65f, SoundGroup.EATING, .5f,
            SoundGroup.BLOCKS, .3f, SoundGroup.ANCHORS, .5f, SoundGroup.PEARLS, .65f,
            SoundGroup.MELEE, .4f, SoundGroup.EQUIPMENT, .4f));
}
