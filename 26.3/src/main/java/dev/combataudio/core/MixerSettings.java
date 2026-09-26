package dev.combataudio.core;

import java.util.EnumMap;
import java.util.Map;

public record MixerSettings(boolean enabled, Map<SoundGroup, Float> gains) {
    public MixerSettings {
        EnumMap<SoundGroup, Float> validated = new EnumMap<>(SoundGroup.class);
        for (SoundGroup group : SoundGroup.values()) {
            float gain = gains.getOrDefault(group, 1.0f);
            if (!Float.isFinite(gain) || gain < 0 || gain > 1.5f) {
                throw new IllegalArgumentException("Gain must be between 0% and 150%: " + group.label);
            }
            validated.put(group, gain);
        }
        gains = Map.copyOf(validated);
    }

    public static MixerSettings defaults() { return new MixerSettings(true, Map.of()); }
    public float gain(SoundGroup group) { return gains.getOrDefault(group, 1.0f); }
    public MixerSettings withEnabled(boolean value) { return new MixerSettings(value, gains); }
    public MixerSettings withGain(SoundGroup group, float value) {
        EnumMap<SoundGroup, Float> copy = new EnumMap<>(SoundGroup.class);
        copy.putAll(gains);
        copy.put(group, value);
        return new MixerSettings(enabled, copy);
    }
}
