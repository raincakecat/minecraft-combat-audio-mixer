package dev.combataudio;

import dev.combataudio.core.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MixerCoreTest {
    @Test void neutralIsUnchanged() { assertEquals(.47f, GainMath.apply(.47f, 1f, true, false)); }
    @Test void attenuationUsesCategoryAdjustedValue() { assertEquals(.14f, GainMath.apply(.4f, .35f, true, false), .00001f); }
    @Test void vanillaMuteAlwaysWins() { assertEquals(0f, GainMath.apply(0, 1.5f, true, false)); }
    @Test void groupMuteWorks() { assertEquals(0f, GainMath.apply(.9f, 0, true, false)); }
    @Test void gainCannotExceedSourceMaximum() { assertEquals(1f, GainMath.apply(.9f, 1.5f, true, false)); }
    @Test void bypassAndDisablePreserveOriginal() {
        assertEquals(.6f, GainMath.apply(.6f, 0, true, true));
        assertEquals(.6f, GainMath.apply(.6f, 0, false, false));
    }
    @Test void settingsAreImmutableSnapshots() {
        var source = new EnumMap<SoundGroup, Float>(SoundGroup.class);
        source.put(SoundGroup.EXPLOSIONS, .3f);
        var settings = new MixerSettings(true, source);
        source.put(SoundGroup.EXPLOSIONS, 0f);
        assertEquals(.3f, settings.gain(SoundGroup.EXPLOSIONS));
        assertThrows(UnsupportedOperationException.class, () -> settings.gains().put(SoundGroup.TOTEMS, 0f));
        assertEquals(1f, settings.gain(SoundGroup.TOTEMS));
    }
    @Test void invalidGainRejected() {
        for (float gain : new float[]{-.1f, 1.51f, Float.NaN, Float.POSITIVE_INFINITY})
            assertThrows(IllegalArgumentException.class, () -> MixerSettings.defaults().withGain(SoundGroup.EXPLOSIONS, gain));
    }
    @Test void eventMappingsAreUnambiguous() {
        var events = new HashSet<String>();
        var groups = new HashSet<String>();
        for (var group : SoundGroup.values()) {
            assertTrue(groups.add(group.key));
            assertFalse(group.events.isEmpty());
            for (String event : group.events) assertTrue(events.add(event), event);
        }
    }
}
