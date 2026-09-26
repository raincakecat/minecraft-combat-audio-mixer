package dev.combataudio;

import dev.combataudio.config.PresetCodec;
import dev.combataudio.core.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PresetCodecTest {
    @Test void roundTripPreservesAllLevelsAndDisabledState() {
        var original = BuiltInPresets.FOCUS.withEnabled(false);
        var result = PresetCodec.decode(PresetCodec.encode("My mix", original));
        assertEquals(original, result.settings());
        assertEquals("My mix", result.name());
        assertTrue(result.warnings().isEmpty());
    }
    @Test void futureSchemaRejected() {
        assertThrows(IllegalArgumentException.class, () -> PresetCodec.decode("{\"schemaVersion\":2,\"gains\":{}}"));
    }
    @Test void missingSchemaRejected() {
        assertThrows(IllegalArgumentException.class, () -> PresetCodec.decode("{\"gains\":{}}"));
    }
    @Test void malformedShapesRejected() {
        for (String input : new String[]{"null", "[]", "oops", "{", "{\"schemaVersion\":1}", "{\"schemaVersion\":1,\"gains\":[]}"})
            assertThrows(IllegalArgumentException.class, () -> PresetCodec.decode(input), input);
    }
    @Test void unknownGroupsWarnAndKnownGroupsSurvive() {
        var result = PresetCodec.decode("{\"schemaVersion\":1,\"mappingVersion\":\"1.21.11-v1\",\"gains\":{\"explosions\":0.25,\"futureSound\":9}}");
        assertEquals(.25f, result.settings().gain(SoundGroup.EXPLOSIONS));
        assertEquals(1f, result.settings().gain(SoundGroup.TOTEMS));
        assertEquals(1, result.warnings().size());
    }
    @Test void nonFiniteAndOutOfRangeValuesRejected() {
        for (String value : new String[]{"-0.1", "1.6", "1e999", "\"0.5\"", "null", "true"})
            assertThrows(IllegalArgumentException.class, () -> PresetCodec.decode("{\"schemaVersion\":1,\"gains\":{\"explosions\":" + value + "}}"));
    }
    @Test void invalidEnabledRejected() {
        assertThrows(IllegalArgumentException.class, () -> PresetCodec.decode("{\"schemaVersion\":1,\"enabled\":\"false\",\"gains\":{}}"));
    }
    @Test void mappingMismatchIsVisible() {
        var result = PresetCodec.decode("{\"schemaVersion\":1,\"mappingVersion\":\"future\",\"gains\":{}}");
        assertFalse(result.warnings().isEmpty());
    }
    @Test void oversizedInputRejected() {
        assertThrows(IllegalArgumentException.class, () -> PresetCodec.decode(" ".repeat(PresetCodec.MAX_BYTES + 1)));
    }
    @Test void filenameTraversalRejected() {
        for (String name : new String[]{"../mix", "..", "C:\\mix", "a/b", " a", "a ", ""}) assertFalse(PresetCodec.validName(name));
        assertTrue(PresetCodec.validName("NA Focus_2"));
    }
}
