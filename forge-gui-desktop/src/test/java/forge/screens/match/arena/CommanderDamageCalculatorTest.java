package forge.screens.match.arena;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CommanderDamageCalculatorTest {
    static class FakeDamageLookup implements CommanderDamageCalculator.DamageLookup {
        final Map<Integer,Integer> map = new HashMap<>();
        public int getDamage(int commanderId) { return map.getOrDefault(commanderId, 0); }
    }

    @Test public void singleCommander_returnsItsDamage() {
        FakeDamageLookup lookup = new FakeDamageLookup();
        lookup.map.put(42, 7);
        int total = CommanderDamageCalculator.sumDamage(Arrays.asList(42), lookup);
        assertEquals(total, 7);
    }
    @Test public void partnerCommanders_sumsBoth() {
        FakeDamageLookup lookup = new FakeDamageLookup();
        lookup.map.put(42, 5); lookup.map.put(99, 3);
        int total = CommanderDamageCalculator.sumDamage(Arrays.asList(42, 99), lookup);
        assertEquals(total, 8);
    }
    @Test public void noCommanders_returnsZero() {
        int total = CommanderDamageCalculator.sumDamage(Collections.emptyList(), new FakeDamageLookup());
        assertEquals(total, 0);
    }
    @Test public void nullCommanderList_returnsZero() {
        int total = CommanderDamageCalculator.sumDamage(null, new FakeDamageLookup());
        assertEquals(total, 0);
    }
    @Test public void isLethal_21OrMore() {
        assertTrue(CommanderDamageCalculator.isLethal(21));
        assertTrue(CommanderDamageCalculator.isLethal(30));
        assertFalse(CommanderDamageCalculator.isLethal(20));
        assertFalse(CommanderDamageCalculator.isLethal(0));
    }
    @Test public void isWarning_18OrMoreButNotLethal() {
        assertTrue(CommanderDamageCalculator.isWarning(18));
        assertTrue(CommanderDamageCalculator.isWarning(20));
        assertFalse(CommanderDamageCalculator.isWarning(17));
        assertFalse(CommanderDamageCalculator.isWarning(21));
    }
}
