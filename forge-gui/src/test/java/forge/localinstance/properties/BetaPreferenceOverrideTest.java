package forge.localinstance.properties;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

public class BetaPreferenceOverrideTest {
    static class PrefsStub {
        Map<String,String> store = new HashMap<>();
        void set(String k, String v) { store.put(k, v); }
        String get(String k) { return store.getOrDefault(k, "false"); }
    }

    @Test
    public void systemPropertyTrue_setsPrefToTrue() {
        PrefsStub prefs = new PrefsStub();
        Properties sys = new Properties();
        sys.setProperty("forge.commander.enhanced", "true");
        boolean applied = BetaPreferenceOverride.apply(prefs::set, sys);
        assertTrue(applied);
        assertEquals(prefs.get("UI_COMMANDER_ENHANCED"), "true");
    }

    @Test
    public void systemPropertyAbsent_returnsFalse_doesNotModifyPrefs() {
        PrefsStub prefs = new PrefsStub();
        Properties sys = new Properties();
        boolean applied = BetaPreferenceOverride.apply(prefs::set, sys);
        assertFalse(applied);
        assertEquals(prefs.get("UI_COMMANDER_ENHANCED"), "false");
    }

    @Test
    public void systemPropertyFalse_returnsFalse() {
        PrefsStub prefs = new PrefsStub();
        Properties sys = new Properties();
        sys.setProperty("forge.commander.enhanced", "false");
        boolean applied = BetaPreferenceOverride.apply(prefs::set, sys);
        assertFalse(applied);
    }
}
