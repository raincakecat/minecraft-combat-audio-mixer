package dev.combataudio.core;

public final class GainMath {
    private GainMath() {}

    public static float apply(float vanillaVolume, float gain, boolean enabled, boolean bypass) {
        if (!enabled || bypass || gain == 1.0f) return vanillaVolume;
        // Vanilla's volume controls have already been applied. Spatial attenuation is
        // untouched, and zero from a vanilla mute always remains zero.
        return Math.max(0.0f, Math.min(1.0f, vanillaVolume * gain));
    }
}
